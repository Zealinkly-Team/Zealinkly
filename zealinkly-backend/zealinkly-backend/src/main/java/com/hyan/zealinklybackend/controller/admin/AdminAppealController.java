package com.hyan.zealinklybackend.controller.admin;

import com.hyan.zealinklybackend.dto.request.ResolveAppealRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.AppealResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.AdminAppealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员 - 申诉列表、处理申诉
 */
@RestController
@RequestMapping("/api/admin/appeals")
@RequiredArgsConstructor
public class AdminAppealController {

    private final AdminAppealService adminAppealService;

    private static void requireAdmin(UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getUserType())) {
            throw new org.springframework.security.access.AccessDeniedException("仅管理员可操作");
        }
    }

    @GetMapping
    public ApiResponse<Page<AppealResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        requireAdmin(principal);
        return ApiResponse.success(adminAppealService.list(status, pageable));
    }

    @GetMapping("/pending")
    public ApiResponse<List<AppealResponse>> pending(@AuthenticationPrincipal UserPrincipal principal) {
        requireAdmin(principal);
        return ApiResponse.success(adminAppealService.listPending());
    }

    @GetMapping("/{id}")
    public ApiResponse<AppealResponse> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        return ApiResponse.success(adminAppealService.getById(id));
    }

    @PatchMapping("/{id}/resolve")
    public ApiResponse<AppealResponse> resolve(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ResolveAppealRequest request) {
        requireAdmin(principal);
        return ApiResponse.success("已处理", adminAppealService.resolve(id, request.getAdminNote()));
    }
}
