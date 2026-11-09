package com.example.campussystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campussystem.dto.ConversationResponse;
import com.example.campussystem.dto.MessageRequest;
import com.example.campussystem.dto.MessageResponse;

import java.util.List;
import java.util.Map;

public interface MessageService {

    MessageResponse sendMessage(MessageRequest request, Long fromUserId);

    List<MessageResponse> getConversationMessages(Long userId, Long contactUserId);

    Page<MessageResponse> getConversationMessagesPage(Long userId, Long contactUserId, int page, int size);

    List<ConversationResponse> getConversationList(Long userId);

    List<MessageResponse> getUnreadMessages(Long userId);

    void markMessageAsRead(Long messageId, Long userId);

    void markConversationAsRead(Long userId, Long contactUserId);

    long getUnreadMessageCount(Long userId);

    List<MessageResponse> getSentMessages(Long userId);

    List<MessageResponse> getReceivedMessages(Long userId);

    List<MessageResponse> getUserMessages(Long userId);

    Page<MessageResponse> getUserMessagesPage(Long userId, int page, int size, String sortBy, String sortDir);

    List<MessageResponse> getProductMessages(Long productId);

    List<MessageResponse> getProductConversationMessages(Long productId, Long userId, Long contactUserId);

    List<MessageResponse> searchMessages(String keyword);

    List<ConversationResponse> getConversations(Long userId);

    List<MessageResponse> searchConversationMessages(Long userId, Long contactUserId, String keyword);

    void deleteConversation(Long userId, Long contactUserId);

    Map<String, Long> getUserMessageStats(Long userId);
}
