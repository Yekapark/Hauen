package com.hauen.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "portfolio")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "area_pyeong", nullable = false)
    private int areaPyeong;

    @Column(name = "construction_start_date", nullable = false)
    private LocalDate constructionStartDate;

    @Column(name = "completion_year", nullable = false)
    private int completionYear;

    @Column(name = "construction_duration_days")
    private Integer constructionDurationDays;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 이미지 목록 (portfolio_image 테이블)
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<PortfolioImage> images = new ArrayList<>();

    // 첫 번째 이미지를 썸네일로 사용
    public String getThumbnailUrl() {
        if (images == null || images.isEmpty()) return null;
        return images.get(0).getImageUrl();
    }

    public boolean hasThumbnail() {
        return images != null && !images.isEmpty();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
