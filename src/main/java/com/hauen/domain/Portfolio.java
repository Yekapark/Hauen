package com.hauen.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "portfolio")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 제목
    @Column(name = "title", nullable = false)
    private String title;

    // 평형
    @Column(name = "area_pyeong", nullable = false)
    private int areaPyeong;

    // 시공일
    @Column(name = "construction_start_date", nullable = false)
    private LocalDate constructionStartDate;

    // 준공연월
    @Column(name = "completion_year", nullable = false)
    private int completionYear;

    // 시공기간
    @Column(name = "construction_duration_days", nullable = false)
    private int constructionDurationDays;

    // 등록일
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 썸네일
    @Column(name = "thumbnail_url", updatable = false)
    private String thumbnailUrl;

    public boolean hasThumbnail() {
        return thumbnailUrl != null && !thumbnailUrl.isEmpty();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();  // 자동 설정
    }
}
