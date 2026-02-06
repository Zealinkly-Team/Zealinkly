package com.hyan.zealinklybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private String username;
    private String userType; // ELDER, VOLUNTEER, ADMIN
}
