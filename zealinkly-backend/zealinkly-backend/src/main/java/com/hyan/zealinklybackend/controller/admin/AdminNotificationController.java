package com.hyan.zealinklybackend.controller.admin;

import com.hyan.zealinklybackend.dto.request.BroadcastRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.AdminNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员 - 应用内广播弹窗通知（老人/志愿者）
 */
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    private static void requireAdmin(UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getUserType())) {
            throw new org.springframework.security.access.AccessDeniedException("仅管理员可操作");
        }
    }

    @PostMapping("/broadcast")
    public ApiResponse<Map<String, Object>> broadcast(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BroadcastRequest request) {
        requireAdmin(principal);
        int count = adminNotificationService.broadcast(
                request.getTargetType(),
                request.getTitle(),
                request.getMessage());
        return ApiResponse.success("已发送", Map.of("sentCount", count));
    }
}
