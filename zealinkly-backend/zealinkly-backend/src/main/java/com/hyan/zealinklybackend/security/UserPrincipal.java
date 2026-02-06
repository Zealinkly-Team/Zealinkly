package com.hyan.zealinklybackend.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户主体（统一封装三种用户类型）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {
    private String userType; // ELDER, VOLUNTEER, ADMIN
    private Long userId;
    private String username;
}
