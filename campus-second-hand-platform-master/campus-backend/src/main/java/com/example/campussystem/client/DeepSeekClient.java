package com.example.campussystem.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek API客户端
 */
@Component
public class DeepSeekClient {
    
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    @Value("${deepseek.api.key}")
    private String apiKey;
    
    @Value("${deepseek.api.base-url}")
    private String baseUrl;
    
    @Value("${deepseek.api.model}")
    private String model;
    
    @Value("${deepseek.api.timeout:60000}")
    private long timeout;
    
    @Value("${deepseek.api.max-tokens:2000}")
    private int maxTokens;
    
    @Value("${deepseek.api.temperature:0.7}")
    private double temperature;
    
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    
    public DeepSeekClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 发送聊天请求到DeepSeek API
     * 
     * @param userMessage 用户消息
     * @param systemPrompt 系统提示词
     * @param conversationHistory 对话历史（可选）
     * @return AI回复内容
     */
    public String chat(String userMessage, String systemPrompt, List<Map<String, String>> conversationHistory) throws IOException {
        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 添加系统提示词
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
        }
        
        // 添加对话历史
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            messages.addAll(conversationHistory);
        }
        
        // 添加当前用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);
        requestBody.put("stream", false);
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        logger.info("DeepSeek API请求: {}", jsonBody);
        
        // 构建HTTP请求
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, JSON))
                .build();
        
        // 发送请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                logger.error("DeepSeek API请求失败: {} - {}", response.code(), errorBody);
                throw new IOException("DeepSeek API请求失败: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            logger.info("DeepSeek API响应: {}", responseBody);
            
            // 解析响应
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            JsonNode choices = jsonResponse.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText();
                    }
                }
            }
            
            throw new IOException("无法从DeepSeek API响应中提取内容");
        }
    }
    
    /**
     * 简化的聊天方法，不需要对话历史
     */
    public String chat(String userMessage, String systemPrompt) throws IOException {
        return chat(userMessage, systemPrompt, null);
    }
    
    /**
     * 最简化的聊天方法，使用默认系统提示词
     */
    public String chat(String userMessage) throws IOException {
        return chat(userMessage, null, null);
    }
}
