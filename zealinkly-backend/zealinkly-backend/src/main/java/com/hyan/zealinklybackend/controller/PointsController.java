package com.hyan.zealinklybackend.controller;

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
 * 积分控制器
 */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    /**
     * 获取积分总数（老人和志愿者可用）
     */
    @GetMapping("/total")
    public ApiResponse<Map<String, Integer>> getTotal(@AuthenticationPrincipal UserPrincipal principal) {
        String userType = principal.getUserType();
        if (!"ELDER".equals(userType) && !"VOLUNTEER".equals(userType)) {
            return ApiResponse.error(403, "仅老人和志愿者可查看积分");
        }
        
        Integer total = pointsService.getPointsTotal(userType, principal.getUserId());
        Map<String, Integer> result = new HashMap<>();
        result.put("total", total);
        return ApiResponse.success(result);
    }

    /**
     * 获取积分流水记录（老人和志愿者可用）
     */
    @GetMapping("/history")
    public ApiResponse<List<PointsLedgerResponse>> getHistory(@AuthenticationPrincipal UserPrincipal principal) {
        String userType = principal.getUserType();
        if (!"ELDER".equals(userType) && !"VOLUNTEER".equals(userType)) {
            return ApiResponse.error(403, "仅老人和志愿者可查看积分流水");
        }
        
        List<PointsLedgerResponse> history = pointsService.getPointsHistory(userType, principal.getUserId());
        return ApiResponse.success(history);
    }
}
