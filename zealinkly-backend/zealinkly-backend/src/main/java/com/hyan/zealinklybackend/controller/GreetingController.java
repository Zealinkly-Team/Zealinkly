package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问候控制器
 */
@RestController
@RequestMapping("/api/greeting")
@RequiredArgsConstructor
public class GreetingController {

    private final UserService userService;

    /**
     * 获取问候语
     * 未登录时返回通用问候，登录后返回个性化问候
     */
    @GetMapping
    public ApiResponse<String> greet(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal != null) {
            var userInfo = userService.getUserInfo(userPrincipal);
            String realName = userInfo != null ? userInfo.getRealName() : null;
            String name = (realName != null && !realName.isEmpty()) ? realName : userPrincipal.getUsername();
            return ApiResponse.success("你好，" + name + "！");
        }
        return ApiResponse.success("你好！");
    }
}
