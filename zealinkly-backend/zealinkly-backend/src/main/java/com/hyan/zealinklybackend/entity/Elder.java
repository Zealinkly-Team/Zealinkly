package com.hyan.zealinklybackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 老人实体类
 */
@Entity
@Table(name = "elders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Elder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(length = 20)
    private String phone;

    private String address;

    @Column(name = "id_card_number", length = 18)
    private String idCardNumber;

    @Column(name = "community_card_number", length = 50)
    private String communityCardNumber;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer points = 0;

    @Column(precision = 10, scale = 8)
    private BigDecimal lat;

    @Column(precision = 11, scale = 8)
    private BigDecimal lng;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (points == null) {
            points = 0;
        }
        if (enabled == null) {
            enabled = true;
        }
    }
}
