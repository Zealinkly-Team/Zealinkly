package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员给老人发放积分
 */
@Data
public class GrantPointsRequest {
    @NotNull(message = "积分数量不能为空")
    private Integer amount;
}
