package com.hauen.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "portfolio_image", indexes = {
        @Index(name = "idx_portfolio_image_portfolio_id", columnList = "portfolio_id"),
        @Index(name = "idx_portfolio_image_portfolio_category", columnList = "portfolio_id,category")
})
public class PortfolioImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 이미지 카테고리
    // thumbnail / before / entrance / living / kitchen / room / bathroom
    @Column(name = "category", nullable = false)
    private String category;

    // R2 공개 URL
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // R2에서 삭제할 때 필요한 key
    @Column(name = "image_key", nullable = false)
    private String imageKey;

    // 정렬 순서
    @Column(name = "sort_order")
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
}
