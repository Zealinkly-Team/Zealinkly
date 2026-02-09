package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.dto.request.HandleEmergencyRequest;
import com.hyan.zealinklybackend.dto.request.TriggerEmergencyRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.EmergencyDetailResponse;
import com.hyan.zealinklybackend.dto.response.TaskResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 紧急报警接口
 */
@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final TaskService taskService;

    /** 老人：一键报警 */
    @PostMapping("/trigger")
    public ApiResponse<TaskResponse> trigger(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TriggerEmergencyRequest request) {
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可发起紧急报警");
        }
        TaskResponse resp = taskService.triggerEmergency(principal.getUserId(), request.getLocation());
        return ApiResponse.success("报警已发出，救援正在路上", resp);
    }

    /** 管理员：待处理报警列表 */
    @GetMapping("/pending")
    public ApiResponse<List<TaskResponse>> pending(@AuthenticationPrincipal UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅管理员可查看待处理报警");
        }
        List<TaskResponse> list = taskService.getPendingEmergencies();
        return ApiResponse.success(list);
    }

    /** 管理员：获取紧急报警详情（包含老人信息、紧急联系人、定位信息） */
    @GetMapping("/{id}/detail")
    public ApiResponse<EmergencyDetailResponse> getDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (!"ADMIN".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅管理员可查看报警详情");
        }
        EmergencyDetailResponse detail = taskService.getEmergencyDetail(id);
        return ApiResponse.success(detail);
    }

    /** 管理员：标记已处理 */
    @PatchMapping("/{id}/handle")
    public ApiResponse<TaskResponse> handle(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody HandleEmergencyRequest request) {
        if (!"ADMIN".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅管理员可处理报警");
        }
        TaskResponse resp = taskService.handleEmergency(id, principal.getUserId(), request.getNote());
        return ApiResponse.success("处理成功", resp);
    }
}
