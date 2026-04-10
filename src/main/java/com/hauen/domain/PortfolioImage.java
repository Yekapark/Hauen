package com.hauen.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "portfolio_image")
public class PortfolioImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // R2 공개 URL
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // R2에서 삭제할 때 필요한 key (예: portfolio/uuid.jpg)
    @Column(name = "image_key", nullable = false)
    private String imageKey;

    // 정렬 순서
    @Column(name = "sort_order")
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
}
