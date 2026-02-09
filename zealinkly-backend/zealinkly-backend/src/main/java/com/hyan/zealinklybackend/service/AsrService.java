package com.hyan.zealinklybackend.service;

import com.baidu.aip.speech.AipSpeech;
import com.hyan.zealinklybackend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * 语音识别服务（百度ASR）
 */
@Slf4j
@Service
public class AsrService {

    private AipSpeech client;

    public AsrService(
            @Value("${app.baidu-asr.app-id:}") String appId,
            @Value("${app.baidu-asr.api-key:}") String apiKey,
            @Value("${app.baidu-asr.secret-key:}") String secretKey) {
        if (appId != null && !appId.isBlank() && 
            apiKey != null && !apiKey.isBlank() && 
            secretKey != null && !secretKey.isBlank()) {
            this.client = new AipSpeech(appId, apiKey, secretKey);
            this.client.setConnectionTimeoutInMillis(5000);
            this.client.setSocketTimeoutInMillis(60000);
            log.info("Baidu ASR client initialized");
        } else {
            log.warn("Baidu ASR credentials not configured, ASR features will be disabled");
        }
    }

    /**
     * 识别语音（短语音识别-中文普通话）
     * @param audioBase64 音频文件base64编码（支持pcm、wav、amr格式）
     * @param format 音频格式，如 "pcm", "wav", "amr"
     * @param rate 采样率，如 16000, 8000
     * @return 识别到的文字内容
     */
    public String recognizeSpeech(String audioBase64, String format, Integer rate) {
        if (client == null) {
            throw new BusinessException("语音识别服务未配置");
        }

        try {
            // 清理base64字符串
            String cleanBase64 = audioBase64.trim();
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
            }
            // 去除所有空白字符
            cleanBase64 = cleanBase64.replaceAll("\\s+", "");
            
            // 检查base64长度（百度ASR限制：base64编码后不超过10M）
            if (cleanBase64.length() > 10 * 1024 * 1024) {
                throw new BusinessException("音频文件过大，base64编码后不能超过10M");
            }
            
            // 将base64字符串转换为byte数组
            byte[] audioBytes = java.util.Base64.getDecoder().decode(cleanBase64);
            
            // 设置参数
            HashMap<String, Object> options = new HashMap<>();
            options.put("dev_pid", 1537); // 1537=中文普通话(纯中文识别)，1737=中文普通话(支持中英文混合)
            // 注意：百度SDK会自动处理channel参数（固定为1，单声道）
            // 如果音频格式不符合要求（采样率、声道数、位深），识别结果可能不准确
            
            log.info("Calling Baidu ASR API, format: {}, rate: {}, audio length: {} bytes ({} KB)", 
                    format, rate, audioBytes.length, audioBytes.length / 1024);
            
            // 调用百度ASR API
            JSONObject res = client.asr(audioBytes, format, rate, options);
            
            // 记录完整响应以便调试
            log.debug("Baidu ASR full response: {}", res.toString(2));
            
            log.info("Baidu ASR response keys: {}", res.keySet());
            
            // 检查错误码
            if (res.has("err_no")) {
                int errNo = res.getInt("err_no");
                if (errNo != 0) {
                    String errMsg = res.optString("err_msg", "未知错误");
                    log.error("Baidu ASR API error: err_no={}, err_msg={}, full response: {}", errNo, errMsg, res.toString());
                    throw new BusinessException("语音识别失败 (错误码: " + errNo + "): " + errMsg);
                }
            }
            
            // 获取识别结果
            if (res.has("result")) {
                org.json.JSONArray resultArray = res.getJSONArray("result");
                if (resultArray != null && resultArray.length() > 0) {
                    // 取第一个结果（通常是最可能的识别结果）
                    String recognizedText = resultArray.getString(0);
                    log.info("Recognized text: {}", recognizedText);
                    
                    // 如果识别结果异常（如只有"嗯嗯嗯"），记录警告
                    if (recognizedText != null && recognizedText.trim().matches("^[嗯啊呃]+[。，！？]*$")) {
                        log.warn("识别结果可能不准确，请检查音频格式：采样率16000Hz/8000Hz，单声道，16bit位深，PCM编码的WAV格式");
                    }
                    
                    return recognizedText.trim();
                }
            }
            
            log.error("ASR response (full): {}", res.toString(2));
            throw new BusinessException("未能识别到语音内容。请确保音频格式正确：采样率16000Hz或8000Hz，单声道，16bit位深，PCM编码的WAV格式。响应: " + res.toString());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("语音识别异常", e);
            throw new BusinessException("语音识别异常: " + e.getMessage());
        }
    }

    /**
     * 识别语音（使用默认参数：wav格式，16000采样率）
     * @param audioBase64 音频文件base64编码
     * @return 识别到的文字内容
     */
    public String recognizeSpeech(String audioBase64) {
        return recognizeSpeech(audioBase64, "wav", 16000);
    }
}
