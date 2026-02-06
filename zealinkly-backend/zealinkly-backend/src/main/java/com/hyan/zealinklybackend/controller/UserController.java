package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.dto.request.UpdateUserInfoRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.UserInfoResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取个人信息
     */
    @GetMapping("/info")
    public ApiResponse<UserInfoResponse> getUserInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserInfoResponse response = userService.getUserInfo(userPrincipal);
        return ApiResponse.success(response);
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/info")
    public ApiResponse<UserInfoResponse> updateUserInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdateUserInfoRequest request) {
        UserInfoResponse response = userService.updateUserInfo(userPrincipal, request);
        return ApiResponse.success("更新成功", response);
    }
}
