package com.hyan.zealinklybackend.dto.response;

import com.hyan.zealinklybackend.entity.Exchange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 兑换记录响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResponse {
    
    private Long id;
    private Long volunteerId;
    private String volunteerName;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer pointsCost;
    private Long adminId;
    private String adminName;
    private LocalDateTime createdAt;
    
    public static ExchangeResponse fromEntity(Exchange exchange) {
        return ExchangeResponse.builder()
                .id(exchange.getId())
                .volunteerId(exchange.getVolunteer().getId())
                .volunteerName(exchange.getVolunteer().getRealName() != null ? 
                        exchange.getVolunteer().getRealName() : exchange.getVolunteer().getUsername())
                .productId(exchange.getProduct().getId())
                .productName(exchange.getProduct().getName())
                .quantity(exchange.getQuantity())
                .pointsCost(exchange.getPointsCost())
                .adminId(exchange.getAdmin().getId())
                .adminName(exchange.getAdmin().getRealName() != null ? 
                        exchange.getAdmin().getRealName() : exchange.getAdmin().getUsername())
                .createdAt(exchange.getCreatedAt())
                .build();
    }
}
