package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.NotificationResponse;
import com.hyan.zealinklybackend.entity.Notification;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知控制器（用户端）
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取通知列表
     */
    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        List<Notification> notifications = notificationService.getNotifications(
                principal.getUserType(),
                principal.getUserId()
        );
        
        List<NotificationResponse> responses = notifications.stream()
                .map(notification -> NotificationResponse.builder()
                        .id(notification.getId())
                        .title(notification.getTitle())
                        .message(notification.getMessage())
                        .isRead(notification.getIsRead())
                        .createdAt(notification.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return ApiResponse.success(responses);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        long count = notificationService.getUnreadCount(
                principal.getUserType(),
                principal.getUserId()
        );
        
        Map<String, Long> result = new HashMap<>();
        result.put("unreadCount", count);
        
        return ApiResponse.success(result);
    }

    /**
     * 标记单条通知为已读
     */
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        
        notificationService.markAsRead(
                id,
                principal.getUserType(),
                principal.getUserId()
        );
        
        return ApiResponse.success("已标记为已读", null);
    }

    /**
     * 标记所有通知为已读
     */
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        notificationService.markAllAsRead(
                principal.getUserType(),
                principal.getUserId()
        );
        
        return ApiResponse.success("已全部标记为已读", null);
    }
}
