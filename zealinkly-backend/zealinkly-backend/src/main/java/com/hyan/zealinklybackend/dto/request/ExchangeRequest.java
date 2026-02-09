package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 兑换商品请求
 */
@Data
public class ExchangeRequest {
    
    @NotNull(message = "志愿者ID不能为空")
    private Long volunteerId;
    
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    @NotNull(message = "兑换数量不能为空")
    @Min(value = 1, message = "兑换数量必须大于0")
    private Integer quantity;
}
