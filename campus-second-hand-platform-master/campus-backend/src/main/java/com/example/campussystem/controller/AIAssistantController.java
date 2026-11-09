package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.dto.AIChatRequest;
import com.example.campussystem.dto.AIChatResponse;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.service.AIAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI智能客服控制器
 */
@RestController
@RequestMapping("/ai-assistant")
@Tag(name = "AI智能客服", description = "AI智能客服相关接口")
public class AIAssistantController {

    @Autowired
    private AIAssistantService aiAssistantService;

    private Long requireUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        return userId;
    }

    @PostMapping("/chat")
    @Operation(summary = "与AI助手聊天", description = "发送消息给AI智能客服助手")
    public ResponseEntity<ApiResponse<AIChatResponse>> chat(
            @Valid @RequestBody AIChatRequest request,
            HttpServletRequest httpRequest) {
        AIChatResponse response = aiAssistantService.chat(request, requireUserId(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/conversation/{sessionId}")
    @Operation(summary = "清除对话历史", description = "清除指定会话的对话历史")
    public ResponseEntity<ApiResponse<Void>> clearConversation(
            @PathVariable String sessionId,
            HttpServletRequest httpRequest) {
        requireUserId(httpRequest);
        aiAssistantService.clearConversationHistory(sessionId);
        return ResponseEntity.ok(ApiResponse.success(null, "对话历史已清除"));
    }

    @GetMapping("/info")
    @Operation(summary = "获取AI助手信息", description = "获取AI智能客服助手的基本信息和能力")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssistantInfo() {
        return ResponseEntity.ok(ApiResponse.success(aiAssistantService.getAssistantInfo()));
    }

    @GetMapping("/health")
    @Operation(summary = "AI助手健康检查", description = "检查AI助手服务是否正常")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = Map.of(
            "status", "ok",
            "service", "AI Assistant",
            "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(ApiResponse.success(health));
    }
}
