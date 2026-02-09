package com.hyan.zealinklybackend.controller.admin;

import com.hyan.zealinklybackend.dto.request.EmergencyContactCreateRequest;
import com.hyan.zealinklybackend.dto.request.EmergencyContactUpdateRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.EmergencyContactResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.EmergencyContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员 - 紧急联系人管理
 */
@RestController
@RequestMapping("/api/admin/elders/{elderId}/emergency-contacts")
@RequiredArgsConstructor
public class AdminEmergencyContactController {

    private final EmergencyContactService emergencyContactService;

    private static void requireAdmin(UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getUserType())) {
            throw new org.springframework.security.access.AccessDeniedException("仅管理员可操作");
        }
    }

    /**
     * 管理员：为老人添加紧急联系人
     */
    @PostMapping
    public ApiResponse<EmergencyContactResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long elderId,
            @Valid @RequestBody EmergencyContactCreateRequest request) {
        requireAdmin(principal);
        EmergencyContactResponse response = emergencyContactService.createByAdmin(elderId, request);
        return ApiResponse.success("添加成功", response);
    }

    /**
     * 管理员：获取老人的紧急联系人列表
     */
    @GetMapping
    public ApiResponse<List<EmergencyContactResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long elderId) {
        requireAdmin(principal);
        List<EmergencyContactResponse> list = emergencyContactService.getByElderId(elderId);
        return ApiResponse.success(list);
    }

    /**
     * 管理员：更新紧急联系人
     */
    @PutMapping("/{contactId}")
    public ApiResponse<EmergencyContactResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long elderId,
            @PathVariable Long contactId,
            @Valid @RequestBody EmergencyContactUpdateRequest request) {
        requireAdmin(principal);
        EmergencyContactResponse response = emergencyContactService.update(contactId, elderId, request);
        return ApiResponse.success("更新成功", response);
    }

    /**
     * 管理员：删除紧急联系人
     */
    @DeleteMapping("/{contactId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long contactId) {
        requireAdmin(principal);
        emergencyContactService.deleteByAdmin(contactId);
        return ApiResponse.success("删除成功", null);
    }
}
