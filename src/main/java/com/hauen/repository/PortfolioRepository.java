package com.hauen.repository;

import com.hauen.domain.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Integer> {
    Page<Portfolio> findByAreaPyeongBetween(int min, int max, Pageable pageable);
    Page<Portfolio> findByAreaPyeongGreaterThanEqual(int min, Pageable pageable);
}
