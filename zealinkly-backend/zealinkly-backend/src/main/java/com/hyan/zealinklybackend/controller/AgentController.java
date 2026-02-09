package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.dto.request.AgentRequest;
import com.hyan.zealinklybackend.dto.request.AgentVoiceRequest;
import com.hyan.zealinklybackend.dto.response.AgentResponse;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.AgentService;
import com.hyan.zealinklybackend.service.AsrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Agent统一入口控制器
 * 接收用户输入，自动识别意图并执行相应操作
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final AsrService asrService;

    /**
     * Agent统一入口：处理用户文本输入，自动识别意图并执行相应操作
     * 
     * 功能：
     * - 如果是互助任务：自动发布任务
     * - 如果是紧急报警：自动触发紧急报警
     * - 如果是AI聊天：自动开始AI对话
     */
    @PostMapping("/process")
    public ApiResponse<AgentResponse> process(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AgentRequest request) {
        
        // 仅老人可以使用Agent
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可使用Agent助手");
        }
        
        log.info("Agent processing text request from elder {}: {}", principal.getUserId(), request.getUserInput());
        
        try {
            AgentResponse response = agentService.processUserInput(
                    principal.getUserId(),
                    request.getUserInput()
            );
            
            return ApiResponse.success(response.getMessage(), response);
            
        } catch (Exception e) {
            log.error("Agent processing failed", e);
            return ApiResponse.error(500, "处理失败: " + e.getMessage());
        }
    }

    /**
     * Agent语音入口：处理用户语音输入，先进行语音识别，然后自动识别意图并执行相应操作
     * 
     * 流程：
     * 1. 语音识别：将音频转换为文字
     * 2. 意图识别：识别用户意图（互助任务/紧急报警/AI聊天）
     * 3. 自动执行：根据意图执行相应操作
     * 
     * 功能：
     * - 如果是互助任务：自动发布任务
     * - 如果是紧急报警：自动触发紧急报警
     * - 如果是AI聊天：自动开始AI对话
     */
    @PostMapping("/process-voice")
    public ApiResponse<AgentResponse> processVoice(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AgentVoiceRequest request) {
        
        // 仅老人可以使用Agent
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可使用Agent助手");
        }
        
        log.info("Agent processing voice request from elder {}", principal.getUserId());
        
        try {
            // 1. 语音识别：将音频转换为文字
            String recognizedText;
            if (request.getFormat() != null && request.getRate() != null) {
                recognizedText = asrService.recognizeSpeech(
                        request.getAudioBase64(),
                        request.getFormat(),
                        request.getRate()
                );
            } else {
                recognizedText = asrService.recognizeSpeech(request.getAudioBase64());
            }
            
            log.info("Voice recognized text: {}", recognizedText);
            
            // 2. 将识别结果传递给Agent处理
            AgentResponse response = agentService.processUserInput(
                    principal.getUserId(),
                    recognizedText
            );
            
            // 3. 在响应中添加识别的文字
            response.setUserInput(recognizedText);
            
            return ApiResponse.success("语音识别并处理成功", response);
            
        } catch (Exception e) {
            log.error("Agent voice processing failed", e);
            return ApiResponse.error(500, "语音处理失败: " + e.getMessage());
        }
    }
}
