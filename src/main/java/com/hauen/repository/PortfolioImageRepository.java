package com.hauen.repository;

import com.hauen.domain.PortfolioImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Integer> {
    List<PortfolioImage> findByPortfolioId(int portfolioId);
}
