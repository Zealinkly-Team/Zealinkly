package com.hyan.zealinklybackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyan.zealinklybackend.entity.AiChat;
import com.hyan.zealinklybackend.entity.Elder;
import com.hyan.zealinklybackend.repository.AiChatRepository;
import com.hyan.zealinklybackend.repository.ElderRepository;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiChatRepository aiChatRepository;
    private final ElderRepository elderRepository;
    private final ObjectMapper objectMapper; // 用于解析JSON

    private static final String API_KEY = "？？？申请一个就行";
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    public String askAi(Long elderId, String question) throws Exception {
        OkHttpClient client = new OkHttpClient();

        // 1. 构建 AI 请求体
        String jsonBody = """
                {
                  "model": "deepseek-chat",
                  "messages": [
                    {"role": "system", "content": "你是一个社区助老志愿者，请用温和、简洁、易懂的语言回答老人的问题。"},
                    {"role": "user", "content": "%s"}
                  ]
                }
                """.formatted(question);

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        // 2. 发送请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("AI服务异常");

            String responseBody = response.body().string();
            // 解析 JSON 获取 AI 回答的内容
            JsonNode node = objectMapper.readTree(responseBody);
            String aiAnswer = node.path("choices").get(0).path("message").path("content").asText();

            // 3. 保存记录到数据库
            Elder elder = elderRepository.findById(elderId).orElse(null);
            AiChat chat = new AiChat();
            chat.setElder(elder);
            chat.setQuestion(question);
            chat.setAnswer(aiAnswer);
            chat.setCreatedAt(LocalDateTime.now());
            aiChatRepository.save(chat);

            return aiAnswer;
        }
    }
}