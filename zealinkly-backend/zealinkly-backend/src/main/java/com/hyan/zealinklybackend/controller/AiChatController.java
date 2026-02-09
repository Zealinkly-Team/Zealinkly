package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.dto.request.AiAskRequest;
import com.hyan.zealinklybackend.dto.response.AiChatItemResponse;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 聊天接口
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final TaskService taskService;

    /** 老人：提问 */
    @PostMapping(value = "/ask", produces = "application/json;charset=UTF-8")
    public ApiResponse<String> ask(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AiAskRequest request) {
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可使用 AI 助手");
        }
        String answer = taskService.askAi(principal.getUserId(), request.getQuestion());
        return ApiResponse.success(answer);
    }

    /** 老人：聊天历史 */
    @GetMapping(value = "/history", produces = "application/json;charset=UTF-8")
    public ApiResponse<List<AiChatItemResponse>> history(@AuthenticationPrincipal UserPrincipal principal) {
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可查看自己的聊天记录");
        }
        List<AiChatItemResponse> list = taskService.getAiChatHistory(principal.getUserId());
        return ApiResponse.success(list);
    }
}
