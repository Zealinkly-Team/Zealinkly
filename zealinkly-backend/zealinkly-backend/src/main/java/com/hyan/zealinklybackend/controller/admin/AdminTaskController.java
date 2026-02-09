package com.hyan.zealinklybackend.controller.admin;

import com.hyan.zealinklybackend.dto.request.AdminTaskUpdateRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.TaskDetailAdminResponse;
import com.hyan.zealinklybackend.dto.response.TaskResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.AdminTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员 - 任务列表、详情（凭证+积分流水）、编辑/审核
 */
@RestController
@RequestMapping("/api/admin/tasks")
@RequiredArgsConstructor
public class AdminTaskController {

    private final AdminTaskService adminTaskService;

    private static void requireAdmin(UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getUserType())) {
            throw new org.springframework.security.access.AccessDeniedException("仅管理员可操作");
        }
    }

    @GetMapping
    public ApiResponse<Page<TaskResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        requireAdmin(principal);
        return ApiResponse.success(adminTaskService.listAll(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskDetailAdminResponse> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        return ApiResponse.success(adminTaskService.getDetail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TaskResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AdminTaskUpdateRequest request) {
        requireAdmin(principal);
        return ApiResponse.success("更新成功", adminTaskService.update(id, request));
    }
}
