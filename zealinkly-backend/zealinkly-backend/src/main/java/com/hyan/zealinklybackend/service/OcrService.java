package com.hyan.zealinklybackend.service;

import com.baidu.aip.ocr.AipOcr;
import com.hyan.zealinklybackend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * OCR识别服务（百度OCR）
 */
@Slf4j
@Service
public class OcrService {

    private AipOcr client;

    public OcrService(
            @Value("${app.baidu-ocr.app-id:}") String appId,
            @Value("${app.baidu-ocr.api-key:}") String apiKey,
            @Value("${app.baidu-ocr.secret-key:}") String secretKey) {
        if (appId != null && !appId.isBlank() && 
            apiKey != null && !apiKey.isBlank() && 
            secretKey != null && !secretKey.isBlank()) {
            this.client = new AipOcr(appId, apiKey, secretKey);
            this.client.setConnectionTimeoutInMillis(5000);
            this.client.setSocketTimeoutInMillis(60000);
            log.info("Baidu OCR client initialized");
        } else {
            log.warn("Baidu OCR credentials not configured, OCR features will be disabled");
        }
    }

    /**
     * 识别身份证号码（正面）
     * @param imageBase64 图片base64编码
     * @return 身份证号码
     */
    public String recognizeIdCard(String imageBase64) {
        if (client == null) {
            throw new BusinessException("OCR服务未配置");
        }

        try {
            // 清理base64字符串
            // 1. 去掉可能的前缀（如 data:image/jpeg;base64,）
            String cleanBase64 = imageBase64.trim();
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
            }
            // 2. 去除所有空白字符（换行符、空格等）
            cleanBase64 = cleanBase64.replaceAll("\\s+", "");
            
            // 检查base64长度（百度OCR限制：base64编码后不超过8M）
            if (cleanBase64.length() > 8 * 1024 * 1024) {
                throw new BusinessException("图片过大，base64编码后不能超过8M");
            }
            
            HashMap<String, String> options = new HashMap<>();
            options.put("detect_direction", "true");
            options.put("detect_risk", "false");
            // 注意：id_card_side已经在方法参数中指定，不需要在options中重复

            log.info("Calling Baidu OCR idcard API, base64 length: {}", cleanBase64.length());
            log.debug("Base64 preview (first 100 chars): {}", cleanBase64.length() > 100 ? cleanBase64.substring(0, 100) + "..." : cleanBase64);
            
            // 调用百度OCR API - 将base64字符串转换为byte数组
            byte[] imageBytes = java.util.Base64.getDecoder().decode(cleanBase64);
            JSONObject res = client.idcard(imageBytes, "front", options);
            
            log.info("Baidu OCR response keys: {}", res.keySet());
            // 立即检查错误码
            if (res.has("error_code")) {
                Object errorCodeObj = res.get("error_code");
                int errorCode = 0;
                try {
                    if (errorCodeObj instanceof Integer) {
                        errorCode = (Integer) errorCodeObj;
                    } else if (errorCodeObj instanceof Number) {
                        errorCode = ((Number) errorCodeObj).intValue();
                    } else {
                        errorCode = Integer.parseInt(errorCodeObj.toString());
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse error_code: {}", errorCodeObj);
                }
                
                if (errorCode != 0) {
                    String errorMsg = res.optString("error_msg", "未知错误");
                    log.error("Baidu OCR API error: code={}, msg={}, full response: {}", errorCode, errorMsg, res.toString());
                    throw new BusinessException("OCR识别失败 (错误码: " + errorCode + "): " + errorMsg);
                }
            }
            
            // 检查是否有错误码（error_code可能不存在、为0或非0）
            if (res.has("error_code")) {
                Object errorCodeObj = res.get("error_code");
                int errorCode = 0;
                try {
                    if (errorCodeObj instanceof Integer) {
                        errorCode = (Integer) errorCodeObj;
                    } else if (errorCodeObj instanceof Number) {
                        errorCode = ((Number) errorCodeObj).intValue();
                    } else {
                        errorCode = Integer.parseInt(errorCodeObj.toString());
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse error_code: {}", errorCodeObj);
                }
                
                // error_code为0表示成功，非0表示失败
                if (errorCode != 0) {
                    String errorMsg = res.optString("error_msg", "OCR识别失败");
                    log.error("Baidu OCR error: code={}, msg={}", errorCode, errorMsg);
                    throw new BusinessException("OCR识别失败: " + errorMsg);
                }
            }
            
            // 检查image_status字段
            String imageStatus = res.optString("image_status", "normal");
            if (!"normal".equals(imageStatus) && !"reversed_side".equals(imageStatus)) {
                log.warn("Image status is not normal: {}", imageStatus);
                throw new BusinessException("身份证图片状态异常: " + imageStatus);
            }
            
            JSONObject wordsResult = res.optJSONObject("words_result");
            if (wordsResult == null || !wordsResult.has("公民身份号码")) {
                // 输出完整的响应以便调试
                try {
                    log.error("OCR response (full): {}", res.toString(2));
                } catch (Exception e) {
                    log.error("OCR response (toString failed): {}", res.toString());
                }
                // 尝试输出所有识别的字段
                if (wordsResult != null) {
                    log.error("Available fields in words_result: {}", wordsResult.keySet());
                } else {
                    log.error("words_result is null");
                }
                // 重新获取image_status用于错误信息
                imageStatus = res.optString("image_status", "unknown");
                log.error("image_status: {}", imageStatus);
                throw new BusinessException("未能识别到身份证号码。图片状态: " + imageStatus + "，请确保图片清晰且包含身份证正面");
            }

            String idCardNumber = wordsResult.getJSONObject("公民身份号码").optString("words", "");
            if (idCardNumber.isEmpty()) {
                throw new BusinessException("身份证号码为空");
            }
            
            log.info("Recognized ID card number: {}", idCardNumber);
            return idCardNumber.trim();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OCR识别异常", e);
            throw new BusinessException("OCR识别异常: " + e.getMessage());
        }
    }

    /**
     * 识别通用文字（用于社区卡号识别）
     * @param imageBase64 图片base64编码
     * @return 识别到的文字内容
     */
    public String recognizeGeneralText(String imageBase64) {
        if (client == null) {
            throw new BusinessException("OCR服务未配置");
        }

        try {
            // 清理base64字符串
            // 1. 去掉可能的前缀（如 data:image/jpeg;base64,）
            String cleanBase64 = imageBase64.trim();
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
            }
            // 2. 去除所有空白字符（换行符、空格等）
            cleanBase64 = cleanBase64.replaceAll("\\s+", "");
            
            // 检查base64长度（百度OCR限制：base64编码后不超过8M）
            if (cleanBase64.length() > 8 * 1024 * 1024) {
                throw new BusinessException("图片过大，base64编码后不能超过8M");
            }
            
            HashMap<String, String> options = new HashMap<>();
            options.put("detect_direction", "true");
            options.put("detect_language", "true");

            log.info("Calling Baidu OCR basicGeneral API, base64 length: {}", cleanBase64.length());
            
            // 调用百度OCR API - 将base64字符串转换为byte数组
            byte[] imageBytes = java.util.Base64.getDecoder().decode(cleanBase64);
            JSONObject res = client.basicGeneral(imageBytes, options);
            log.info("Baidu OCR response keys: {}", res.keySet());
            
            // 检查是否有错误码（error_code可能不存在、为0或非0）
            if (res.has("error_code")) {
                Object errorCodeObj = res.get("error_code");
                int errorCode = 0;
                try {
                    if (errorCodeObj instanceof Integer) {
                        errorCode = (Integer) errorCodeObj;
                    } else if (errorCodeObj instanceof Number) {
                        errorCode = ((Number) errorCodeObj).intValue();
                    } else {
                        errorCode = Integer.parseInt(errorCodeObj.toString());
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse error_code: {}", errorCodeObj);
                }
                
                // error_code为0表示成功，非0表示失败
                if (errorCode != 0) {
                    String errorMsg = res.optString("error_msg", "OCR识别失败");
                    log.error("Baidu OCR error: code={}, msg={}", errorCode, errorMsg);
                    throw new BusinessException("OCR识别失败: " + errorMsg);
                }
            }

            StringBuilder text = new StringBuilder();
            if (res.has("words_result")) {
                org.json.JSONArray wordsResult = res.optJSONArray("words_result");
                if (wordsResult != null) {
                    for (int i = 0; i < wordsResult.length(); i++) {
                        JSONObject word = wordsResult.optJSONObject(i);
                        if (word != null && word.has("words")) {
                            text.append(word.optString("words", "")).append(" ");
                        }
                    }
                }
            }

            String result = text.toString().trim();
            if (result.isEmpty()) {
                log.error("OCR response (full): {}", res.toString(2));
                log.error("words_result_num: {}", res.optInt("words_result_num", 0));
                throw new BusinessException("未能识别到文字内容，请确保图片清晰。响应: " + res.toString());
            }
            
            log.info("Recognized text: {}", result);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OCR识别异常", e);
            throw new BusinessException("OCR识别异常: " + e.getMessage());
        }
    }

    /**
     * 从识别结果中提取社区卡号（简单实现：提取数字串）
     * @param recognizedText OCR识别的文字
     * @return 社区卡号（可能是数字串）
     */
    public String extractCardNumber(String recognizedText) {
        // 简单实现：提取最长的数字串作为卡号
        String[] parts = recognizedText.split("\\s+");
        String longestNumber = "";
        for (String part : parts) {
            String numbers = part.replaceAll("[^0-9]", "");
            if (numbers.length() > longestNumber.length()) {
                longestNumber = numbers;
            }
        }
        return longestNumber;
    }
}
