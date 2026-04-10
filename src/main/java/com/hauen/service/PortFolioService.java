package com.hauen.service;

import com.hauen.domain.Portfolio;
import com.hauen.domain.PortfolioImage;
import com.hauen.repository.PortfolioImageRepository;
import com.hauen.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortFolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final R2Service r2Service;

    public Page<Portfolio> findByFilter(String filter, Pageable pageable) {
        return switch (filter) {
            case "20" -> portfolioRepository.findByAreaPyeongBetween(20, 29, pageable);
            case "30" -> portfolioRepository.findByAreaPyeongBetween(30, 39, pageable);
            case "40" -> portfolioRepository.findByAreaPyeongGreaterThanEqual(40, pageable);
            default   -> portfolioRepository.findAll(pageable);
        };
    }

    public Portfolio findById(int id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오 없음: " + id));
    }

    // 신규 등록
    @Transactional
    public void save(Portfolio portfolio, List<MultipartFile> files) throws IOException {
        // 먼저 저장해서 PK 확보
        portfolioRepository.save(portfolio);
        uploadImages(portfolio, files, portfolio.getImages().size());
    }

    // 수정 (기존 정보 업데이트 + 새 이미지 추가)
    @Transactional
    public void update(int id, Portfolio form, List<MultipartFile> newFiles, List<Integer> deleteImageIds) throws IOException {
        Portfolio portfolio = findById(id);

        // 기본 정보 업데이트
        portfolio.setTitle(form.getTitle());
        portfolio.setAreaPyeong(form.getAreaPyeong());
        portfolio.setConstructionStartDate(form.getConstructionStartDate());
        portfolio.setCompletionYear(form.getCompletionYear());
        portfolio.setConstructionDurationDays(form.getConstructionDurationDays());

        // 선택한 이미지 삭제
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            List<PortfolioImage> toDelete = portfolio.getImages().stream()
                    .filter(img -> deleteImageIds.contains(img.getId()))
                    .toList();
            toDelete.forEach(img -> r2Service.delete(img.getImageKey()));
            portfolio.getImages().removeAll(toDelete);
            portfolioImageRepository.deleteAllById(deleteImageIds);
        }

        // 새 이미지 추가
        if (newFiles != null && !newFiles.isEmpty()) {
            uploadImages(portfolio, newFiles, portfolio.getImages().size());
        }
    }

    // 삭제 (R2 이미지 + DB)
    @Transactional
    public void delete(int id) {
        Portfolio portfolio = findById(id);

        List<String> keys = portfolio.getImages().stream()
                .map(PortfolioImage::getImageKey)
                .toList();
        r2Service.deleteAll(keys);

        portfolioRepository.delete(portfolio);
    }

    // 공통 이미지 업로드 처리
    private void uploadImages(Portfolio portfolio, List<MultipartFile> files, int startOrder) throws IOException {
        int order = startOrder;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            R2Service.UploadResult result = r2Service.uploadWithKey(file, portfolio.getId(), portfolio.getTitle());

            PortfolioImage image = new PortfolioImage();
            image.setPortfolio(portfolio);
            image.setImageKey(result.key());
            image.setImageUrl(result.url());
            image.setSortOrder(order++);
            portfolio.getImages().add(image);
        }
    }
}
