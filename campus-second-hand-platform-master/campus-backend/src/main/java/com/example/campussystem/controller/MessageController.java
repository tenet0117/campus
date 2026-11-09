package com.example.campussystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.dto.ConversationResponse;
import com.example.campussystem.dto.MessageRequest;
import com.example.campussystem.dto.MessageResponse;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    private Long requireUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        return userId;
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody MessageRequest request,
            HttpServletRequest httpRequest) {
        MessageResponse response = messageService.sendMessage(request, requireUserId(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/conversation/{contactUserId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getConversationMessages(
            @PathVariable Long contactUserId,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getConversationMessages(requireUserId(request), contactUserId)));
    }

    @GetMapping("/conversation/{contactUserId}/page")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getConversationMessagesPage(
            @PathVariable Long contactUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getConversationMessagesPage(requireUserId(request), contactUserId, page, size)));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversationList(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getConversationList(requireUserId(request))));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getUnreadMessages(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getUnreadMessages(requireUserId(request))));
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(
            @PathVariable Long messageId,
            HttpServletRequest request) {
        messageService.markMessageAsRead(messageId, requireUserId(request));
        return ResponseEntity.ok(ApiResponse.success(null, "消息已标记为已读"));
    }

    @PutMapping("/conversation/{contactUserId}/read")
    public ResponseEntity<ApiResponse<Void>> markConversationAsRead(
            @PathVariable Long contactUserId,
            HttpServletRequest request) {
        messageService.markConversationAsRead(requireUserId(request), contactUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "对话消息已标记为已读"));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getUnreadMessageCount(requireUserId(request))));
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getSentMessages(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getSentMessages(requireUserId(request))));
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getReceivedMessages(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getReceivedMessages(requireUserId(request))));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getUserMessages(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getUserMessages(requireUserId(request))));
    }

    @GetMapping("/all/page")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getUserMessagesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getUserMessagesPage(requireUserId(request), page, size, sortBy, sortDir)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getProductMessages(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getProductMessages(productId)));
    }

    @GetMapping("/product/{productId}/conversation/{contactUserId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getProductConversationMessages(
            @PathVariable Long productId,
            @PathVariable Long contactUserId,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getProductConversationMessages(productId, requireUserId(request), contactUserId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> searchMessages(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(messageService.searchMessages(keyword)));
    }

    @GetMapping("/conversations/updates")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversationUpdates(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getConversations(requireUserId(request))));
    }

    @GetMapping("/conversation/{contactUserId}/search")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> searchConversationMessages(
            @PathVariable Long contactUserId,
            @RequestParam String keyword,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.searchConversationMessages(requireUserId(request), contactUserId, keyword)));
    }

    @DeleteMapping("/conversation/{contactUserId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable Long contactUserId,
            HttpServletRequest request) {
        messageService.deleteConversation(requireUserId(request), contactUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "对话已删除"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUserMessageStats(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getUserMessageStats(requireUserId(request))));
    }

    @GetMapping("/unread/total")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTotalUnreadCount(HttpServletRequest request) {
        long totalUnread = messageService.getUnreadMessageCount(requireUserId(request));
        Map<String, Object> result = new HashMap<>();
        result.put("totalUnread", totalUnread);
        result.put("hasUnread", totalUnread > 0);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long messageId,
            HttpServletRequest request) {
        messageService.markMessageAsRead(messageId, requireUserId(request));
        return ResponseEntity.ok(ApiResponse.success(null, "消息已标记为已读"));
    }
}
