package com.hyan.zealinklybackend.service.intent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyan.zealinklybackend.dto.response.TodoItem;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 意图识别服务（使用LangChain4j编程式AI服务）
 * 自动识别用户输入的意图，分类为：互助任务、紧急报警、AI聊天
 * 支持从长文本中提取多个任务并生成待办清单
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntentRecognitionService {

    private final ChatLanguageModel chatLanguageModel;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * AI服务接口定义 - 简单意图识别
     */
    private interface IntentRecognitionAiService {
        @SystemMessage("你是一个意图识别助手。根据用户输入的一句话，判断用户的意图。" +
                "可能的意图类型有：" +
                "1. MUTUAL_AID - 互助任务：用户需要帮助完成某个任务（如买东西、送东西、陪伴等）" +
                "2. EMERGENCY - 紧急报警：用户遇到紧急情况需要立即帮助（如摔倒、生病、迷路等）" +
                "3. AI_CHAT - AI聊天：用户只是想聊天、咨询问题、获取信息等" +
                "" +
                "请只返回意图类型（MUTUAL_AID、EMERGENCY或AI_CHAT），不要返回其他内容。")
        String recognizeIntent(@UserMessage String userInput);
    }
    
    /**
     * AI服务接口定义 - 任务提取
     */
    private interface TaskExtractionAiService {
        @SystemMessage("你是一个任务提取助手。从老人的长段话中提取出需要帮助的任务。" +
                "任务类型：" +
                "1. MUTUAL_AID - 互助任务：需要帮助完成的任务（如买东西、送东西、陪伴、修理等）" +
                "2. EMERGENCY - 紧急报警：紧急情况需要立即帮助（如摔倒、生病、迷路、身体不适等）" +
                "" +
                "优先级：" +
                "- HIGH：紧急报警类任务，需要立即处理" +
                "- MEDIUM：重要的互助任务" +
                "- LOW：一般的互助任务" +
                "" +
                "请以JSON数组格式返回提取的任务，格式如下：" +
                "[{\"type\":\"MUTUAL_AID\",\"description\":\"任务描述\",\"priority\":\"MEDIUM\"}," +
                "{\"type\":\"EMERGENCY\",\"description\":\"紧急情况描述\",\"priority\":\"HIGH\"}]" +
                "" +
                "如果没有提取到任务，返回空数组 []。" +
                "只返回JSON数组，不要返回其他内容。")
        String extractTasks(@UserMessage String userInput);
    }
    
    private IntentRecognitionAiService aiService;
    private TaskExtractionAiService taskExtractionService;
    
    @PostConstruct
    public void init() {
        this.aiService = AiServices.create(IntentRecognitionAiService.class, chatLanguageModel);
        this.taskExtractionService = AiServices.create(TaskExtractionAiService.class, chatLanguageModel);
        log.info("IntentRecognitionService initialized successfully");
    }

    /**
     * 识别用户意图（简单版本，用于短句）
     * @param userInput 用户输入的一句话
     * @return 意图类型：MUTUAL_AID（互助任务）、EMERGENCY（紧急报警）、AI_CHAT（AI聊天）
     */
    public String recognizeIntent(String userInput) {
        try {
            return aiService.recognizeIntent(userInput);
        } catch (Exception e) {
            log.error("Intent recognition failed for input: {}", userInput, e);
            throw e;
        }
    }
    
    /**
     * 从长文本中提取任务清单
     * @param userInput 用户输入的长段话
     * @return 提取的任务列表
     */
    public List<TodoItem> extractTasks(String userInput) {
        try {
            String jsonResponse = taskExtractionService.extractTasks(userInput);
            log.debug("Task extraction JSON response: {}", jsonResponse);
            
            // 清理响应，移除可能的markdown代码块标记
            jsonResponse = jsonResponse.trim();
            if (jsonResponse.startsWith("```json")) {
                jsonResponse = jsonResponse.substring(7);
            }
            if (jsonResponse.startsWith("```")) {
                jsonResponse = jsonResponse.substring(3);
            }
            if (jsonResponse.endsWith("```")) {
                jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
            }
            jsonResponse = jsonResponse.trim();
            
            // 解析JSON数组
            List<TaskJson> taskJsons = objectMapper.readValue(jsonResponse, new TypeReference<List<TaskJson>>() {});
            
            // 转换为TodoItem列表
            List<TodoItem> tasks = new ArrayList<>();
            for (TaskJson taskJson : taskJsons) {
                String typeDescription = switch (taskJson.type) {
                    case "MUTUAL_AID" -> "互助任务";
                    case "EMERGENCY" -> "紧急报警";
                    default -> "未知";
                };
                
                TodoItem item = TodoItem.builder()
                        .type(taskJson.type)
                        .typeDescription(typeDescription)
                        .description(taskJson.description)
                        .priority(taskJson.priority != null ? taskJson.priority : "MEDIUM")
                        .build();
                tasks.add(item);
            }
            
            log.info("Extracted {} tasks from user input", tasks.size());
            return tasks;
            
        } catch (Exception e) {
            log.error("Task extraction failed for input: {}", userInput, e);
            return new ArrayList<>(); // 返回空列表，不抛出异常
        }
    }
    
    /**
     * 临时类用于JSON解析
     */
    private static class TaskJson {
        public String type;
        public String description;
        public String priority;
    }
}
