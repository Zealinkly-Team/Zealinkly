package com.hyan.zealinklybackend.controller.admin;

import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.PointsLedgerResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 积分管理控制器
 */
@RestController
@RequestMapping("/api/admin/points")
@RequiredArgsConstructor
public class AdminPointsController {

    private final PointsService pointsService;

    private static void requireAdmin(UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getUserType())) {
            throw new org.springframework.security.access.AccessDeniedException("仅管理员可操作");
        }
    }

    /**
     * 管理员：查看用户的积分总数
     */
    @GetMapping("/users/{userType}/{userId}/total")
    public ApiResponse<Map<String, Integer>> getPointsTotal(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String userType,
            @PathVariable Long userId) {
        requireAdmin(principal);
        
        if (!"ELDER".equals(userType) && !"VOLUNTEER".equals(userType)) {
            return ApiResponse.error(400, "用户类型必须是ELDER或VOLUNTEER");
        }
        
        Integer total = pointsService.getPointsTotal(userType, userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("total", total);
        return ApiResponse.success(result);
    }

    /**
     * 管理员：查看用户的积分流水
     */
    @GetMapping("/users/{userType}/{userId}/history")
    public ApiResponse<List<PointsLedgerResponse>> getPointsHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String userType,
            @PathVariable Long userId) {
        requireAdmin(principal);
        
        if (!"ELDER".equals(userType) && !"VOLUNTEER".equals(userType)) {
            return ApiResponse.error(400, "用户类型必须是ELDER或VOLUNTEER");
        }
        
        List<PointsLedgerResponse> history = pointsService.getPointsHistory(userType, userId);
        return ApiResponse.success(history);
    }
}
