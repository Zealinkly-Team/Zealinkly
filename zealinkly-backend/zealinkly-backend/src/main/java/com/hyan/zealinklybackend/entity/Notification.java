package com.hyan.zealinklybackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * 应用内通知（含广播弹窗）
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receiver_type", nullable = false, length = 20)
    private String receiverType; // ELDER, VOLUNTEER, ADMIN

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
}
