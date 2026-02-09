package com.hyan.zealinklybackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * 申诉：针对某任务的申诉，由管理员处理
 */
@Entity
@Table(name = "appeals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "complainant_type", nullable = false, length = 20)
    private String complainantType; // ELDER, VOLUNTEER

    @Column(name = "complainant_id", nullable = false)
    private Long complainantId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, RESOLVED

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}
