package com.hyan.zealinklybackend.controller.admin;

import com.hyan.zealinklybackend.dto.request.ScanCardRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.OcrService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员OCR工具控制器
 */
@RestController
@RequestMapping("/api/admin/ocr")
@RequiredArgsConstructor
public class AdminOcrController {

    private final OcrService ocrService;

    /**
     * 识别卡片号码（仅识别，不查找用户）
     */
    @PostMapping("/recognize")
    public ApiResponse<CardRecognitionResponse> recognize(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ScanCardRequest request) {
        requireAdmin(principal);
        
        String cardNumber;
        String cardType = request.getCardType() != null ? request.getCardType().toUpperCase() : null;

        // OCR识别
        if ("ID_CARD".equals(cardType)) {
            cardNumber = ocrService.recognizeIdCard(request.getImageBase64());
            cardType = "ID_CARD";
        } else if ("COMMUNITY_CARD".equals(cardType)) {
            String text = ocrService.recognizeGeneralText(request.getImageBase64());
            cardNumber = ocrService.extractCardNumber(text);
            if (cardNumber.isEmpty()) {
                throw new com.hyan.zealinklybackend.exception.BusinessException("未能识别到社区卡号");
            }
            cardType = "COMMUNITY_CARD";
        } else {
            // 自动识别
            try {
                cardNumber = ocrService.recognizeIdCard(request.getImageBase64());
                cardType = "ID_CARD";
            } catch (Exception e) {
                String text = ocrService.recognizeGeneralText(request.getImageBase64());
                cardNumber = ocrService.extractCardNumber(text);
                if (cardNumber.isEmpty()) {
                    throw new com.hyan.zealinklybackend.exception.BusinessException("未能识别到卡片信息");
                }
                cardType = "COMMUNITY_CARD";
            }
        }

        CardRecognitionResponse response = new CardRecognitionResponse();
        response.setCardNumber(cardNumber);
        response.setCardType(cardType);
        
        return ApiResponse.success(response);
    }

    private void requireAdmin(UserPrincipal principal) {
        if (!"ADMIN".equals(principal.getUserType())) {
            throw new com.hyan.zealinklybackend.exception.BusinessException("仅管理员可访问");
        }
    }

    @Data
    public static class CardRecognitionResponse {
        private String cardNumber;
        private String cardType;
    }
}
