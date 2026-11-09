package com.example.campussystem.service;

import com.example.campussystem.dto.MessageResponse;

public interface WebSocketService {

    void sendNewMessageNotification(Long userId, MessageResponse payload);

    void sendNewOrderNotification(Long sellerId, Object payload);

    void sendOrderUpdateNotification(Long userId, Object payload);

    void sendToUser(Long userId, String destination, Object payload);
}
