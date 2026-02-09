package com.hyan.zealinklybackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * 积分流水（时间银行账本）
 */
@Entity
@Table(name = "points_ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_type", nullable = false, length = 20)
    private String userType; // ELDER, VOLUNTEER

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "balance_after")
    private Integer balanceAfter;

    @Column(length = 50)
    private String reason; // TASK_REWARD, TASK_COST, GIFT_EXCHANGE, ADJUSTMENT

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_id")
    private Exchange exchange;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
