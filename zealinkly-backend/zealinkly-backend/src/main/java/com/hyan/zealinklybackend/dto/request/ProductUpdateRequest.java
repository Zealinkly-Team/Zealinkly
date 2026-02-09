package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新商品请求
 */
@Data
public class ProductUpdateRequest {
    
    @Size(max = 100, message = "商品名称长度不能超过100")
    private String name;
    
    @Size(max = 1000, message = "商品描述长度不能超过1000")
    private String description;
    
    @Min(value = 1, message = "积分价格必须大于0")
    private Integer pointsPrice;
    
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;
    
    private String imageUrl;
    
    private Boolean enabled;
}
