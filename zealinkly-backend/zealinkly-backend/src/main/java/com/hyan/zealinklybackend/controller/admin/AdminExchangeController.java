package com.hyan.zealinklybackend.controller.admin;

import com.hyan.zealinklybackend.dto.request.ExchangeRequest;
import com.hyan.zealinklybackend.dto.request.ScanCardRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.ExchangeResponse;
import com.hyan.zealinklybackend.dto.response.VolunteerDetailResponse;
import com.hyan.zealinklybackend.entity.Volunteer;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.VolunteerRepository;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.ExchangeService;
import com.hyan.zealinklybackend.service.OcrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员兑换管理控制器
 */
@RestController
@RequestMapping("/api/admin/exchanges")
@RequiredArgsConstructor
public class AdminExchangeController {

    private final ExchangeService exchangeService;
    private final OcrService ocrService;
    private final VolunteerRepository volunteerRepository;

    /**
     * 兑换商品（管理员操作）
     */
    @PostMapping("/exchange")
    public ApiResponse<ExchangeResponse> exchange(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ExchangeRequest request) {
        requireAdmin(principal);
        return ApiResponse.success("兑换成功", exchangeService.exchange(principal.getUserId(), request));
    }

    /**
     * 获取兑换记录列表（分页）
     */
    @GetMapping
    public ApiResponse<Page<ExchangeResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin(principal);
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(exchangeService.listAll(pageable));
    }

    /**
     * 获取兑换记录详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ExchangeResponse> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        requireAdmin(principal);
        return ApiResponse.success(exchangeService.getById(id));
    }

    /**
     * 扫描志愿者卡片，返回志愿者ID（用于兑换时自动填充）
     */
    @PostMapping("/scan-card")
    public ApiResponse<VolunteerDetailResponse> scanCard(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ScanCardRequest request) {
        requireAdmin(principal);
        
        String cardNumber;
        String cardType = request.getCardType() != null ? request.getCardType().toUpperCase() : null;

        // OCR识别
        if ("ID_CARD".equals(cardType)) {
            cardNumber = ocrService.recognizeIdCard(request.getImageBase64());
        } else if ("COMMUNITY_CARD".equals(cardType)) {
            String text = ocrService.recognizeGeneralText(request.getImageBase64());
            cardNumber = ocrService.extractCardNumber(text);
            if (cardNumber.isEmpty()) {
                throw new BusinessException("未能识别到社区卡号");
            }
        } else {
            // 自动识别
            try {
                cardNumber = ocrService.recognizeIdCard(request.getImageBase64());
                cardType = "ID_CARD";
            } catch (Exception e) {
                String text = ocrService.recognizeGeneralText(request.getImageBase64());
                cardNumber = ocrService.extractCardNumber(text);
                if (cardNumber.isEmpty()) {
                    throw new BusinessException("未能识别到卡片信息");
                }
                cardType = "COMMUNITY_CARD";
            }
        }

        // 查找志愿者
        Volunteer volunteer = null;
        if ("ID_CARD".equals(cardType)) {
            volunteer = volunteerRepository.findByIdCardNumber(cardNumber)
                    .orElse(null);
        } else {
            volunteer = volunteerRepository.findByCommunityCardNumber(cardNumber)
                    .orElse(null);
        }

        if (volunteer == null) {
            throw new BusinessException("未找到对应的志愿者");
        }

        return ApiResponse.success(VolunteerDetailResponse.fromEntity(volunteer));
    }

    private void requireAdmin(UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getUserType())) {
            throw new BusinessException("仅管理员可访问");
        }
    }
}
