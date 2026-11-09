package com.example.campussystem.service;

import com.example.campussystem.dto.AIChatRequest;
import com.example.campussystem.dto.AIChatResponse;

import java.util.Map;

public interface AIAssistantService {

    AIChatResponse chat(AIChatRequest request, Long userId);

    void clearConversationHistory(String sessionId);

    Map<String, Object> getAssistantInfo();
}
