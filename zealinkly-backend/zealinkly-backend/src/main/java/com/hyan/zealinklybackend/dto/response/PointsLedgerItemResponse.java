package com.hyan.zealinklybackend.dto.response;

import com.hyan.zealinklybackend.entity.PointsLedger;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class PointsLedgerItemResponse {
    private Long id;
    private String userType;
    private Long userId;
    private Integer amount;
    private Integer balanceAfter;
    private String reason;
    private Long taskId;
    private OffsetDateTime createdAt;

    public static PointsLedgerItemResponse fromEntity(PointsLedger l) {
        if (l == null) return null;
        return PointsLedgerItemResponse.builder()
                .id(l.getId())
                .userType(l.getUserType())
                .userId(l.getUserId())
                .amount(l.getAmount())
                .balanceAfter(l.getBalanceAfter())
                .reason(l.getReason())
                .taskId(l.getTask() != null ? l.getTask().getId() : null)
                .createdAt(l.getCreatedAt())
                .build();
    }
}
