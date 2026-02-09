package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.dto.request.CardLoginRequest;
import com.hyan.zealinklybackend.dto.request.LoginRequest;
import com.hyan.zealinklybackend.dto.request.RegisterRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.LoginResponse;
import com.hyan.zealinklybackend.dto.response.RegisterResponse;
import com.hyan.zealinklybackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 注册老人
     */
    @PostMapping("/register/elder")
    public ApiResponse<RegisterResponse> registerElder(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.registerElder(request);
        return ApiResponse.success("注册成功", response);
    }

    /**
     * 注册志愿者
     */
    @PostMapping("/register/volunteer")
    public ApiResponse<RegisterResponse> registerVolunteer(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.registerVolunteer(request);
        return ApiResponse.success("注册成功", response);
    }

    /**
     * 注册管理员
     */
    @PostMapping("/register/admin")
    public ApiResponse<RegisterResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.registerAdmin(request);
        return ApiResponse.success("注册成功", response);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("登录成功", response);
    }

    /**
     * 卡片登录（身份证或社区卡）
     */
    @PostMapping("/login-by-card")
    public ApiResponse<LoginResponse> loginByCard(@Valid @RequestBody CardLoginRequest request) {
        LoginResponse response = authService.loginByCard(request);
        return ApiResponse.success("登录成功", response);
    }
}
