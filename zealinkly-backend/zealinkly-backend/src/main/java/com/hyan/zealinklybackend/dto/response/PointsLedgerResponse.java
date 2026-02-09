package com.hyan.zealinklybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 积分流水响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsLedgerResponse {
    private Long id;
    private Integer amount; // 积分变动数量（正数为增加，负数为减少）
    private Integer balanceAfter; // 变动后余额
    private String reason; // 变动原因
    private Long taskId; // 关联的任务ID（如果有）
    private Long exchangeId; // 关联的兑换ID（如果有）
    private OffsetDateTime createdAt;
    
    /**
     * 原因的中文描述
     */
    public String getReasonDescription() {
        if (reason == null) return "未知";
        return switch (reason) {
            case "TASK_REWARD" -> "任务奖励";
            case "TASK_COST" -> "任务消耗";
            case "GIFT_EXCHANGE" -> "礼品兑换";
            case "ADJUSTMENT" -> "管理员调整";
            case "MONTHLY_GRANT" -> "月度发放";
            case "ADMIN_GRANT" -> "管理员发放";
            default -> reason;
        };
    }
}
