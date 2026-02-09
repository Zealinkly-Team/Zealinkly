package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 语音识别请求
 */
@Data
public class SpeechRecognitionRequest {
    /**
     * 音频文件base64编码
     */
    @NotBlank(message = "音频数据不能为空")
    private String audioBase64;
    
    /**
     * 音频格式，如 "pcm", "wav", "amr"（可选，默认为 "wav"）
     */
    private String format = "wav";
    
    /**
     * 采样率，如 16000, 8000（可选，默认为 16000）
     */
    private Integer rate = 16000;
}
