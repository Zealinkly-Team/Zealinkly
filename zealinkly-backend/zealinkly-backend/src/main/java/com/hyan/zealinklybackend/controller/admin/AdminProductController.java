package com.hyan.zealinklybackend.controller.admin;

import com.hyan.zealinklybackend.dto.request.ProductCreateRequest;
import com.hyan.zealinklybackend.dto.request.ProductUpdateRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.ProductResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员商品管理控制器
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    /**
     * 获取商品列表（分页）
     */
    @GetMapping
    public ApiResponse<Page<ProductResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin(principal);
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(adminProductService.listAll(pageable));
    }

    /**
     * 获取已启用商品列表（分页）
     */
    @GetMapping("/enabled")
    public ApiResponse<Page<ProductResponse>> listEnabled(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin(principal);
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(adminProductService.listEnabled(pageable));
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        return ApiResponse.success(adminProductService.getById(id));
    }

    /**
     * 创建商品
     */
    @PostMapping
    public ApiResponse<ProductResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ProductCreateRequest request) {
        requireAdmin(principal);
        return ApiResponse.success("创建成功", adminProductService.create(request));
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        requireAdmin(principal);
        return ApiResponse.success("更新成功", adminProductService.update(id, request));
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        adminProductService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    private void requireAdmin(UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getUserType())) {
            throw new com.hyan.zealinklybackend.exception.BusinessException("仅管理员可访问");
        }
    }
}
