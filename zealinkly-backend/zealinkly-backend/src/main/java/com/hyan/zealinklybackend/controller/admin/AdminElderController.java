package com.hyan.zealinklybackend.controller.admin;

import com.hyan.zealinklybackend.dto.request.AdminElderCreateRequest;
import com.hyan.zealinklybackend.dto.request.AdminElderUpdateRequest;
import com.hyan.zealinklybackend.dto.request.GrantPointsRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.BulkImportResult;
import com.hyan.zealinklybackend.dto.response.ElderDetailResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.AdminElderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 管理员 - 老人信息 CRUD、批量导入/删除、禁用/解封
 */
@RestController
@RequestMapping("/api/admin/elders")
@RequiredArgsConstructor
public class AdminElderController {

    private final AdminElderService adminElderService;

    private static void requireAdmin(UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getUserType())) {
            throw new org.springframework.security.access.AccessDeniedException("仅管理员可操作");
        }
    }

    @GetMapping
    public ApiResponse<Page<ElderDetailResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Boolean enabled,
            @PageableDefault(size = 20) Pageable pageable) {
        requireAdmin(principal);
        return ApiResponse.success(adminElderService.list(enabled, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<ElderDetailResponse> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        return ApiResponse.success(adminElderService.getById(id));
    }

    @PostMapping
    public ApiResponse<ElderDetailResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AdminElderCreateRequest request) {
        requireAdmin(principal);
        return ApiResponse.success("创建成功", adminElderService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ElderDetailResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AdminElderUpdateRequest request) {
        requireAdmin(principal);
        return ApiResponse.success("更新成功", adminElderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        adminElderService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/bulk-delete")
    public ApiResponse<Void> bulkDelete(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody List<Long> ids) {
        requireAdmin(principal);
        adminElderService.bulkDelete(ids);
        return ApiResponse.success("批量删除成功", null);
    }

    @PostMapping("/bulk-import")
    public ApiResponse<BulkImportResult> bulkImport(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        requireAdmin(principal);
        return ApiResponse.success(adminElderService.bulkImport(file));
    }

    @PatchMapping("/{id}/disable")
    public ApiResponse<ElderDetailResponse> disable(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        return ApiResponse.success("已禁用", adminElderService.disable(id));
    }

    @PatchMapping("/{id}/enable")
    public ApiResponse<ElderDetailResponse> enable(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        return ApiResponse.success("已解封", adminElderService.enable(id));
    }

    /** 管理员给老人发放积分 */
    @PostMapping("/{id}/grant-points")
    public ApiResponse<ElderDetailResponse> grantPoints(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody GrantPointsRequest request) {
        requireAdmin(principal);
        return ApiResponse.success("发放成功", adminElderService.grantPoints(id, request.getAmount()));
    }
}
