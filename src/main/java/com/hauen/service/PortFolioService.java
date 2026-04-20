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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortFolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final R2Service r2Service;

    @Transactional(readOnly = true)
    public Page<Portfolio> findByFilter(String filter, Pageable pageable) {
        // 1단계: 페이징으로 ID 조회 (컬렉션 fetch 없이 카운트/정렬만)
        Page<Portfolio> page = switch (filter) {
            case "20" -> portfolioRepository.findByAreaPyeongBetween(0, 29, pageable);
            case "30" -> portfolioRepository.findByAreaPyeongBetween(30, 39, pageable);
            case "40" -> portfolioRepository.findByAreaPyeongGreaterThanEqual(40, pageable);
            default   -> portfolioRepository.findAll(pageable);
        };

        if (page.isEmpty()) return page;

        // 2단계: 해당 ID들의 이미지 전체를 한 번에 JOIN FETCH (N+1 방지)
        List<Integer> ids = page.getContent().stream().map(Portfolio::getId).toList();
        Map<Integer, Portfolio> fetchedMap = portfolioRepository.findWithImagesByIds(ids).stream()
                .collect(Collectors.toMap(Portfolio::getId, p -> p));

        // 페이지 순서 유지
        List<Portfolio> ordered = ids.stream()
                .map(fetchedMap::get)
                .filter(p -> p != null)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(ordered, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Portfolio findById(int id) {
        return portfolioRepository.findByIdWithImages(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오 없음: " + id));
    }

    // 신규 등록 (카테고리별)
    @Transactional
    public void save(Portfolio portfolio, Map<String, List<MultipartFile>> categoryFiles) throws IOException {
        portfolioRepository.save(portfolio);
        for (Map.Entry<String, List<MultipartFile>> entry : categoryFiles.entrySet()) {
            uploadImagesByCategory(portfolio, entry.getValue(), entry.getKey());
        }
    }

    // 수정 (기존 정보 업데이트 + 새 이미지 추가)
    @Transactional
    public void update(int id, Portfolio form, Map<String, List<MultipartFile>> categoryFiles, List<Integer> deleteImageIds) throws IOException {
        Portfolio portfolio = findById(id);

        portfolio.setTitle(form.getTitle());
        portfolio.setAreaPyeong(form.getAreaPyeong());
        portfolio.setConstructionStartDate(form.getConstructionStartDate());
        portfolio.setCompletionYear(form.getCompletionYear());
        portfolio.setConstructionDurationDays(form.getConstructionDurationDays());

        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            List<PortfolioImage> toDelete = portfolio.getImages().stream()
                    .filter(img -> deleteImageIds.contains(img.getId()))
                    .toList();
            toDelete.forEach(img -> r2Service.delete(img.getImageKey()));
            portfolio.getImages().removeAll(toDelete);
            portfolioImageRepository.deleteAllById(deleteImageIds);
        }

        if (categoryFiles != null) {
            for (Map.Entry<String, List<MultipartFile>> entry : categoryFiles.entrySet()) {
                uploadImagesByCategory(portfolio, entry.getValue(), entry.getKey());
            }
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

    // 카테고리별 이미지 업로드
    private void uploadImagesByCategory(Portfolio portfolio, List<MultipartFile> files, String category) throws IOException {
        if (files == null) return;
        int order = (int) portfolio.getImages().stream().filter(img -> category.equals(img.getCategory())).count();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            R2Service.UploadResult result = r2Service.uploadWithKey(file, portfolio.getId(), portfolio.getTitle(), category);
            PortfolioImage image = new PortfolioImage();
            image.setPortfolio(portfolio);
            image.setCategory(category);
            image.setImageKey(result.key());
            image.setImageUrl(result.url());
            image.setSortOrder(order++);
            portfolio.getImages().add(image);
        }
    }
}
