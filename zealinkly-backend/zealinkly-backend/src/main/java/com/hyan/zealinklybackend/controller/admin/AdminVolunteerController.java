package com.hyan.zealinklybackend.controller.admin;

import com.hyan.zealinklybackend.dto.request.AdminVolunteerCreateRequest;
import com.hyan.zealinklybackend.dto.request.AdminVolunteerUpdateRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.BulkImportResult;
import com.hyan.zealinklybackend.dto.response.VolunteerDetailResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.AdminVolunteerService;
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
 * 管理员 - 志愿者信息 CRUD、批量导入/删除、禁用/解封
 */
@RestController
@RequestMapping("/api/admin/volunteers")
@RequiredArgsConstructor
public class AdminVolunteerController {

    private final AdminVolunteerService adminVolunteerService;

    private static void requireAdmin(UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getUserType())) {
            throw new org.springframework.security.access.AccessDeniedException("仅管理员可操作");
        }
    }

    @GetMapping
    public ApiResponse<Page<VolunteerDetailResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Boolean enabled,
            @PageableDefault(size = 20) Pageable pageable) {
        requireAdmin(principal);
        return ApiResponse.success(adminVolunteerService.list(enabled, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<VolunteerDetailResponse> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        return ApiResponse.success(adminVolunteerService.getById(id));
    }

    @PostMapping
    public ApiResponse<VolunteerDetailResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AdminVolunteerCreateRequest request) {
        requireAdmin(principal);
        return ApiResponse.success("创建成功", adminVolunteerService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<VolunteerDetailResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AdminVolunteerUpdateRequest request) {
        requireAdmin(principal);
        return ApiResponse.success("更新成功", adminVolunteerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        adminVolunteerService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/bulk-delete")
    public ApiResponse<Void> bulkDelete(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody List<Long> ids) {
        requireAdmin(principal);
        adminVolunteerService.bulkDelete(ids);
        return ApiResponse.success("批量删除成功", null);
    }

    @PostMapping("/bulk-import")
    public ApiResponse<BulkImportResult> bulkImport(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        requireAdmin(principal);
        return ApiResponse.success(adminVolunteerService.bulkImport(file));
    }

    @PatchMapping("/{id}/disable")
    public ApiResponse<VolunteerDetailResponse> disable(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        return ApiResponse.success("已禁用", adminVolunteerService.disable(id));
    }

    @PatchMapping("/{id}/enable")
    public ApiResponse<VolunteerDetailResponse> enable(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        return ApiResponse.success("已解封", adminVolunteerService.enable(id));
    }

    @PostMapping("/{id}/grant-points")
    public ApiResponse<VolunteerDetailResponse> grantPoints(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody com.hyan.zealinklybackend.dto.request.GrantPointsRequest request) {
        requireAdmin(principal);
        return ApiResponse.success("发放成功", adminVolunteerService.grantPoints(id, request.getAmount()));
    }
}
