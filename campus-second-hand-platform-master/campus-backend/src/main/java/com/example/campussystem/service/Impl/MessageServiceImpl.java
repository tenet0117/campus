package com.example.campussystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campussystem.dto.ConversationResponse;
import com.example.campussystem.dto.MessageRequest;
import com.example.campussystem.dto.MessageResponse;
import com.example.campussystem.entity.Message;
import com.example.campussystem.entity.Product;
import com.example.campussystem.entity.User;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.mapper.MessageMapper;
import com.example.campussystem.mapper.ProductMapper;
import com.example.campussystem.mapper.UserMapper;
import com.example.campussystem.service.MessageService;
import com.example.campussystem.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Transactional
    public MessageResponse sendMessage(MessageRequest request, Long fromUserId) {
        System.out.println("发送消息调试: fromUserId=" + fromUserId + ", toUserId=" + request.getToUserId());

        User toUser = userMapper.selectById(request.getToUserId());
        if (toUser == null) throw new BusinessException("接收者不存在");

        User fromUser = userMapper.selectById(fromUserId);
        if (fromUser == null) throw new BusinessException("发送者不存在");

        if (fromUser.getId().equals(toUser.getId())) {
            throw new BusinessException("不能给自己发送消息");
        }

        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(request.getToUserId());
        message.setProductId(request.getProductId());
        message.setContent(request.getContent());
        message.setType(request.getType());
        message.setIsRead(0);

        messageMapper.insert(message);
        MessageResponse response = convertToResponse(message);

        try {
            webSocketService.sendNewMessageNotification(request.getToUserId(), response);
        } catch (Exception e) {
            System.err.println("发送WebSocket通知失败: " + e.getMessage());
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(Long userId, Long contactUserId) {
        userMapper.selectById(contactUserId);
        List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromUserId, userId).eq(Message::getToUserId, contactUserId)
                         .or(q -> q.eq(Message::getFromUserId, contactUserId).eq(Message::getToUserId, userId)))
                .orderByAsc(Message::getCreateTime));
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversationMessagesPage(Long userId, Long contactUserId, int page, int size) {
        userMapper.selectById(contactUserId);

        Page<Message> mpPage = new Page<>(page, size);
        Page<Message> messagePage = messageMapper.selectPage(mpPage, new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromUserId, userId).eq(Message::getToUserId, contactUserId)
                         .or(q -> q.eq(Message::getFromUserId, contactUserId).eq(Message::getToUserId, userId)))
                .orderByDesc(Message::getCreateTime));

        Page<MessageResponse> result = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        result.setRecords(messagePage.getRecords().stream().map(this::convertToResponse).collect(Collectors.toList()));
        return result;
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationList(Long userId) {
        // 获取用户参与的所有消息，按联系人分组取最新一条
        List<Message> allMessages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromUserId, userId).or().eq(Message::getToUserId, userId))
                .orderByDesc(Message::getCreateTime));

        Map<Long, Message> latestMap = new LinkedHashMap<>();
        for (Message msg : allMessages) {
            Long contactId = msg.getFromUserId().equals(userId) ? msg.getToUserId() : msg.getFromUserId();
            latestMap.putIfAbsent(contactId, msg);
        }

        List<ConversationResponse> conversations = new ArrayList<>();
        for (Map.Entry<Long, Message> entry : latestMap.entrySet()) {
            Long contactUserId = entry.getKey();
            Message message = entry.getValue();

            User contact = userMapper.selectById(contactUserId);
            if (contact == null) continue;

            long unreadCount = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                    .eq(Message::getFromUserId, contactUserId)
                    .eq(Message::getToUserId, userId)
                    .eq(Message::getIsRead, 0));

            ConversationResponse conversation = new ConversationResponse();
            conversation.setContactUserId(contactUserId);
            conversation.setContactUsername(contact.getUsername());
            conversation.setContactNickname(contact.getNickname());
            conversation.setContactAvatar(contact.getAvatar());
            conversation.setLastMessageContent(message.getContent());
            conversation.setLastMessageType(message.getType());
            conversation.setLastMessageTime(message.getCreateTime());
            conversation.setUnreadCount(unreadCount);

            if (message.getProductId() != null) {
                Product product = productMapper.selectById(message.getProductId());
                if (product != null) {
                    conversation.setProductId(product.getId());
                    conversation.setProductTitle(product.getTitle());
                    conversation.setProductImage(product.getImages());
                }
            }
            conversations.add(conversation);
        }
        return conversations;
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getUnreadMessages(Long userId) {
        List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getIsRead, 0)
                .orderByDesc(Message::getCreateTime));
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public void markMessageAsRead(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) throw new BusinessException("消息不存在");
        if (!message.getToUserId().equals(userId)) throw new BusinessException("无权限操作此消息");

        message.markAsRead();
        messageMapper.updateById(message);
    }

    public void markConversationAsRead(Long userId, Long contactUserId) {
        userMapper.selectById(contactUserId);

        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .set(Message::getIsRead, 1)
                .set(Message::getReadTime, LocalDateTime.now())
                .eq(Message::getFromUserId, contactUserId)
                .eq(Message::getToUserId, userId)
                .eq(Message::getIsRead, 0));
    }

    @Transactional(readOnly = true)
    public long getUnreadMessageCount(Long userId) {
        return messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getIsRead, 0));
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getSentMessages(Long userId) {
        List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getFromUserId, userId)
                .orderByDesc(Message::getCreateTime));
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getReceivedMessages(Long userId) {
        List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .orderByDesc(Message::getCreateTime));
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getUserMessages(Long userId) {
        List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromUserId, userId).or().eq(Message::getToUserId, userId))
                .orderByDesc(Message::getCreateTime));
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getUserMessagesPage(Long userId, int page, int size, String sortBy, String sortDir) {
        Page<Message> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromUserId, userId).or().eq(Message::getToUserId, userId));

        if ("ASC".equalsIgnoreCase(sortDir)) {
            wrapper.orderByAsc(Message::getCreateTime);
        } else {
            wrapper.orderByDesc(Message::getCreateTime);
        }

        Page<Message> messagePage = messageMapper.selectPage(mpPage, wrapper);
        Page<MessageResponse> result = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        result.setRecords(messagePage.getRecords().stream().map(this::convertToResponse).collect(Collectors.toList()));
        return result;
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getProductMessages(Long productId) {
        productMapper.selectById(productId);
        List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getProductId, productId)
                .orderByDesc(Message::getCreateTime));
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getProductConversationMessages(Long productId, Long userId, Long contactUserId) {
        productMapper.selectById(productId);
        userMapper.selectById(contactUserId);

        List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getProductId, productId)
                .and(w -> w.eq(Message::getFromUserId, userId).eq(Message::getToUserId, contactUserId)
                         .or(q -> q.eq(Message::getFromUserId, contactUserId).eq(Message::getToUserId, userId)))
                .orderByAsc(Message::getCreateTime));
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> searchMessages(String keyword) {
        List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .like(Message::getContent, keyword));
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(Long userId) {
        return getConversationList(userId);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> searchConversationMessages(Long userId, Long contactUserId, String keyword) {
        userMapper.selectById(contactUserId);

        List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .like(Message::getContent, keyword)
                .and(w -> w.eq(Message::getFromUserId, userId).eq(Message::getToUserId, contactUserId)
                         .or(q -> q.eq(Message::getFromUserId, contactUserId).eq(Message::getToUserId, userId))));
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public void deleteConversation(Long userId, Long contactUserId) {
        userMapper.selectById(contactUserId);
        messageMapper.delete(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromUserId, userId).eq(Message::getToUserId, contactUserId)
                         .or(q -> q.eq(Message::getFromUserId, contactUserId).eq(Message::getToUserId, userId))));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getUserMessageStats(Long userId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("sent", messageMapper.selectCount(new LambdaQueryWrapper<Message>().eq(Message::getFromUserId, userId)));
        stats.put("received", messageMapper.selectCount(new LambdaQueryWrapper<Message>().eq(Message::getToUserId, userId)));
        stats.put("unread", messageMapper.selectCount(new LambdaQueryWrapper<Message>().eq(Message::getToUserId, userId).eq(Message::getIsRead, 0)));
        return stats;
    }

    private MessageResponse convertToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setFromUserId(message.getFromUserId());
        response.setToUserId(message.getToUserId());
        response.setProductId(message.getProductId());
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setTypeText(getTypeText(message.getType()));
        response.setIsRead(message.getIsRead());
        response.setCreateTime(message.getCreateTime());
        response.setReadTime(message.getReadTime());
        response.setIsUnread(message.getIsRead() == 0);
        response.setReadStatus(getReadStatusText(message.getIsRead(), message.getReadTime()));

        if (message.getFromUser() != null) {
            response.setFromUsername(message.getFromUser().getUsername());
            response.setFromNickname(message.getFromUser().getNickname());
            response.setFromAvatar(message.getFromUser().getAvatar());
        } else {
            User fromUser = userMapper.selectById(message.getFromUserId());
            if (fromUser != null) {
                response.setFromUsername(fromUser.getUsername());
                response.setFromNickname(fromUser.getNickname());
                response.setFromAvatar(fromUser.getAvatar());
            }
        }

        if (message.getToUser() != null) {
            response.setToUsername(message.getToUser().getUsername());
            response.setToNickname(message.getToUser().getNickname());
            response.setToAvatar(message.getToUser().getAvatar());
        } else {
            User toUser = userMapper.selectById(message.getToUserId());
            if (toUser != null) {
                response.setToUsername(toUser.getUsername());
                response.setToNickname(toUser.getNickname());
                response.setToAvatar(toUser.getAvatar());
            }
        }

        if (message.getProductId() != null) {
            if (message.getProduct() != null) {
                response.setProductTitle(message.getProduct().getTitle());
                response.setProductImage(message.getProduct().getImages());
            } else {
                Product product = productMapper.selectById(message.getProductId());
                if (product != null) {
                    response.setProductTitle(product.getTitle());
                    response.setProductImage(product.getImages());
                }
            }
        }

        return response;
    }

    private String getTypeText(Integer type) {
        switch (type == null ? 0 : type) {
            case 1: return "文本";
            case 2: return "图片";
            default: return "未知类型";
        }
    }

    private String getReadStatusText(Integer isRead, LocalDateTime readTime) {
        if (isRead == null || isRead == 0) return "未读";
        if (readTime != null) return "已读(" + readTime.toString() + ")";
        return "已读";
    }
}
