package com.hauen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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

    // 파일 업로드 → UploadResult 반환 (디렉토리: {pk}_{title})
    public UploadResult uploadWithKey(MultipartFile file, int portfolioId, String portfolioTitle) throws IOException {
        String dir = buildDir(portfolioId, portfolioTitle);
        String key = dir + "/" + UUID.randomUUID() + "." + getExtension(file.getOriginalFilename());

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );
        return new UploadResult(key, publicUrl + "/" + key);
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
