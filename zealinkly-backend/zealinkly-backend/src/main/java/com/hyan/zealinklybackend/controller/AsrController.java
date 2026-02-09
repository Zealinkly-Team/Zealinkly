package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.dto.request.SpeechRecognitionRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.SpeechRecognitionResponse;
import com.hyan.zealinklybackend.service.AsrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 语音识别控制器
 */
@RestController
@RequestMapping("/api/asr")
@RequiredArgsConstructor
public class AsrController {

    private final AsrService asrService;

    /**
     * 语音转文字（老人和志愿者可用）
     */
    @PostMapping("/recognize")
    public ApiResponse<SpeechRecognitionResponse> recognize(@Valid @RequestBody SpeechRecognitionRequest request) {
        String text;
        if (request.getFormat() != null && request.getRate() != null) {
            text = asrService.recognizeSpeech(request.getAudioBase64(), request.getFormat(), request.getRate());
        } else {
            text = asrService.recognizeSpeech(request.getAudioBase64());
        }
        
        SpeechRecognitionResponse response = SpeechRecognitionResponse.builder()
                .text(text)
                .build();
        
        return ApiResponse.success(response);
    }
}
