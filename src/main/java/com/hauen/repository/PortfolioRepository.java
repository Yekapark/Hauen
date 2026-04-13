package com.hauen.repository;

import com.hauen.domain.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Integer> {

    Page<Portfolio> findByAreaPyeongBetween(int min, int max, Pageable pageable);
    Page<Portfolio> findByAreaPyeongGreaterThanEqual(int min, Pageable pageable);

    // ID 목록으로 images 포함 조회 (썸네일용 - thumbnail 카테고리만)
    @Query("SELECT DISTINCT p FROM Portfolio p LEFT JOIN FETCH p.images i WHERE p.id IN :ids AND i.category = 'thumbnail'")
    List<Portfolio> findWithThumbnailsByIds(@Param("ids") List<Integer> ids);
}
