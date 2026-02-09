package com.hyan.zealinklybackend.controller;

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
 * 紧急联系人控制器（老人自己管理）
 */
@RestController
@RequestMapping("/api/emergency-contacts")
@RequiredArgsConstructor
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;

    /**
     * 老人：添加紧急联系人
     */
    @PostMapping
    public ApiResponse<EmergencyContactResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody EmergencyContactCreateRequest request) {
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可管理自己的紧急联系人");
        }
        EmergencyContactResponse response = emergencyContactService.create(principal.getUserId(), request);
        return ApiResponse.success("添加成功", response);
    }

    /**
     * 老人：获取自己的紧急联系人列表
     */
    @GetMapping
    public ApiResponse<List<EmergencyContactResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可查看自己的紧急联系人");
        }
        List<EmergencyContactResponse> list = emergencyContactService.getByElderId(principal.getUserId());
        return ApiResponse.success(list);
    }

    /**
     * 老人：更新紧急联系人
     */
    @PutMapping("/{id}")
    public ApiResponse<EmergencyContactResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody EmergencyContactUpdateRequest request) {
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可修改自己的紧急联系人");
        }
        EmergencyContactResponse response = emergencyContactService.update(id, principal.getUserId(), request);
        return ApiResponse.success("更新成功", response);
    }

    /**
     * 老人：删除紧急联系人
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可删除自己的紧急联系人");
        }
        emergencyContactService.delete(id, principal.getUserId());
        return ApiResponse.success("删除成功", null);
    }
}
