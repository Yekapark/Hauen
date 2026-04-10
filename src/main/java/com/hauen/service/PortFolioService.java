package com.hauen.service;

import com.hauen.domain.Portfolio;
import com.hauen.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PortFolioService {

    private final PortfolioRepository portfolioRepository;

    public Page<Portfolio> findByFilter(String filter, Pageable pageable) {
        return switch (filter) {
            case "20" -> portfolioRepository.findByAreaPyeongBetween(20, 29, pageable);
            case "30" -> portfolioRepository.findByAreaPyeongBetween(30, 39, pageable);
            case "40" -> portfolioRepository.findByAreaPyeongGreaterThanEqual(40, pageable);
            default   -> portfolioRepository.findAll(pageable);
        };
    }

    public void save(Portfolio portfolio) {
        portfolioRepository.save(portfolio);
    }
}
