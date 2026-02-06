package com.hyan.zealinklybackend.dto.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新个人信息请求 DTO
 */
@Data
public class UpdateUserInfoRequest {
    private String realName;
    private String phone;
    
    // 老人专用字段
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;
    
    // 管理员专用字段
    private Integer roleLevel;
}
