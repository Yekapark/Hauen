package com.hauen.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class R2Service {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    // 워터마크 이미지 (한 번만 로드)
    private BufferedImage watermarkImage;

    // 파일 업로드 → UploadResult 반환 (디렉토리: {pk}_{title}/{category})
    public UploadResult uploadWithKey(MultipartFile file, int portfolioId, String portfolioTitle, String category) throws IOException {
        String dir = buildDir(portfolioId, portfolioTitle) + "/" + category;
        String ext = getExtension(file.getOriginalFilename());
        String key = dir + "/" + UUID.randomUUID() + "." + ext;

        // 썸네일: orientation만 보정, 그 외: orientation 보정 후 워터마크 합성
        byte[] imageBytes;
        if ("thumbnail".equals(category)) {
            imageBytes = correctOrientation(file.getBytes(), ext);
        } else {
            imageBytes = applyWatermark(file.getBytes(), ext);
        }

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(imageBytes)
        );
        return new UploadResult(key, publicUrl + "/" + key);
    }

    // 하위 호환 (category 없는 버전)
    public UploadResult uploadWithKey(MultipartFile file, int portfolioId, String portfolioTitle) throws IOException {
        return uploadWithKey(file, portfolioId, portfolioTitle, "etc");
    }

    // 단일 삭제
    public void delete(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
    }

    // 다중 삭제
    public void deleteAll(List<String> keys) {
        keys.forEach(this::delete);
    }

    // ── EXIF orientation 보정 후 워터마크 합성 ──
    private byte[] applyWatermark(byte[] originalBytes, String ext) throws IOException {
        BufferedImage original = readWithOrientation(originalBytes);
        if (original == null) return originalBytes;

        BufferedImage wm = getWatermark();
        if (wm == null) return encodeImage(original, ext);

        int origW = original.getWidth();
        int origH = original.getHeight();

        // 워터마크 크기: 원본 너비의 25%
        int wmTargetW = (int) (origW * 0.25);
        int wmTargetH = (int) (wm.getHeight() * ((double) wmTargetW / wm.getWidth()));

        // 위치: 중앙 하단, 하단에서 3% 여백
        int x = (origW - wmTargetW) / 2;
        int y = origH - wmTargetH - (int) (origH * 0.03);

        // 합성
        BufferedImage result = new BufferedImage(origW, origH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(original, 0, 0, null);

        // 워터마크 투명도 70%
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
        g.setComposite(ac);
        g.drawImage(wm, x, y, wmTargetW, wmTargetH, null);
        g.dispose();

        return encodeImage(result, ext);
    }

    // EXIF orientation 보정만 (썸네일용)
    private byte[] correctOrientation(byte[] originalBytes, String ext) throws IOException {
        BufferedImage image = readWithOrientation(originalBytes);
        if (image == null) return originalBytes;
        return encodeImage(image, ext);
    }

    // EXIF orientation을 읽어 올바르게 회전된 BufferedImage 반환
    private BufferedImage readWithOrientation(byte[] imageBytes) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) return null;

        int orientation = 1;
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(imageBytes));
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
        } catch (Exception ignored) {
        }

        return applyOrientation(image, orientation);
    }

    // EXIF orientation 값에 따라 이미지 회전/반전
    private BufferedImage applyOrientation(BufferedImage image, int orientation) {
        if (orientation == 1) return image;

        int w = image.getWidth();
        int h = image.getHeight();
        boolean swapDimensions = orientation >= 5;
        int destW = swapDimensions ? h : w;
        int destH = swapDimensions ? w : h;

        AffineTransform transform = new AffineTransform();
        switch (orientation) {
            case 2 -> { transform.scale(-1, 1); transform.translate(-w, 0); }
            case 3 -> { transform.translate(w, h); transform.rotate(Math.PI); }
            case 4 -> { transform.scale(1, -1); transform.translate(0, -h); }
            case 5 -> { transform.rotate(-Math.PI / 2); transform.scale(-1, 1); }
            case 6 -> { transform.translate(h, 0); transform.rotate(Math.PI / 2); }
            case 7 -> { transform.scale(-1, 1); transform.translate(-h, 0); transform.rotate(Math.PI / 2); }
            case 8 -> { transform.translate(0, w); transform.rotate(-Math.PI / 2); }
            default -> { return image; }
        }

        BufferedImage dest = new BufferedImage(destW, destH,
                image.getType() != 0 ? image.getType() : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dest.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.transform(transform);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return dest;
    }

    // 이미지를 바이트 배열로 인코딩 (jpg는 ARGB 불가 → RGB 변환)
    private byte[] encodeImage(BufferedImage image, String ext) throws IOException {
        String format = ext.equalsIgnoreCase("png") ? "png" : "jpg";
        BufferedImage output = image;
        if (format.equals("jpg") && image.getType() != BufferedImage.TYPE_INT_RGB) {
            output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = output.createGraphics();
            g.drawImage(image, 0, 0, Color.WHITE, null);
            g.dispose();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(output, format, baos);
        return baos.toByteArray();
    }

    // 워터마크 이미지 로드 (캐싱)
    private BufferedImage getWatermark() {
        if (watermarkImage != null) return watermarkImage;
        try {
            ClassPathResource resource = new ClassPathResource("static/images/watermark.png");
            watermarkImage = ImageIO.read(resource.getInputStream());
        } catch (IOException e) {
            // watermark.png 없으면 그냥 원본 사용
        }
        return watermarkImage;
    }

    // {pk}_{title} 형식 디렉토리명 생성 (특수문자 제거)
    private String buildDir(int id, String title) {
        String safeTitle = title.replaceAll("[^a-zA-Z0-9가-힣]", "_");
        return id + "_" + safeTitle;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public record UploadResult(String key, String url) {}
}
