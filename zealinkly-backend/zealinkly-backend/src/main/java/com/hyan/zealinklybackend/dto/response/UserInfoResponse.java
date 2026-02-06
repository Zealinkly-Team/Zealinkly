package com.hyan.zealinklybackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 用户信息响应 DTO
 */
@Data
public class UserInfoResponse {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String userType; // ELDER, VOLUNTEER, ADMIN
    
    // 老人字段
    private Integer points;
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;
    
    // 志愿者字段
    private Boolean idCardStatus;
    
    // 管理员字段
    private Integer roleLevel;
    
    private OffsetDateTime createdAt;
}
