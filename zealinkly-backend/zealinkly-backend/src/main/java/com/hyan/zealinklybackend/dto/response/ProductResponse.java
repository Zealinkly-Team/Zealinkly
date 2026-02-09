package com.hyan.zealinklybackend.dto.response;

import com.hyan.zealinklybackend.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商品响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private Long id;
    private String name;
    private String description;
    private Integer pointsPrice;
    private Integer stock;
    private String imageUrl;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ProductResponse fromEntity(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .pointsPrice(product.getPointsPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .enabled(product.getEnabled())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
