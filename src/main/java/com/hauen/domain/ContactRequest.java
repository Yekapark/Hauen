package com.hauen.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "contact_request", indexes = {
        @Index(name = "idx_contact_phone_created", columnList = "phone,created_at"),
        @Index(name = "idx_contact_ip_created",    columnList = "client_ip,created_at")
})
public class ContactRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String location;

    private String buildingType;
    private String area;
    private String jungmun;
    private String expansion;
    private String bathroom;
    private String sink;
    private String builtin;
    private String budget;
    private String startDate;
    private String moveInDate;

    @Column(length = 1000)
    private String message;

    @Column(name = "client_ip")
    private String clientIp;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
