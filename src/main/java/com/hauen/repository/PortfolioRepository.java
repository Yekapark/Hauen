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

    // ID 목록으로 images 전체 JOIN FETCH (getThumbnailUrl()은 Java에서 필터)
    @Query("SELECT DISTINCT p FROM Portfolio p LEFT JOIN FETCH p.images WHERE p.id IN :ids")
    List<Portfolio> findWithImagesByIds(@Param("ids") List<Integer> ids);

    // 상세 페이지용 - 이미지 전체 한 번에 로드
    @Query("SELECT DISTINCT p FROM Portfolio p LEFT JOIN FETCH p.images WHERE p.id = :id")
    java.util.Optional<Portfolio> findByIdWithImages(@Param("id") int id);
}
