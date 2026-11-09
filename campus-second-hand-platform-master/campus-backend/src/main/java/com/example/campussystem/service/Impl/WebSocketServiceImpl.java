package com.example.campussystem.service.impl;

import com.example.campussystem.dto.MessageResponse;
import com.example.campussystem.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServiceImpl.class);

    // 用户队列目的地（前端应订阅 /user/queue/new-message）
    public static final String USER_NEW_MESSAGE_QUEUE = "/queue/new-message";
    
    // 新订单通知队列（前端应订阅 /user/queue/new-order）
    public static final String USER_NEW_ORDER_QUEUE = "/queue/new-order";
    
    // 订单状态更新通知队列（前端应订阅 /user/queue/order-update）
    public static final String USER_ORDER_UPDATE_QUEUE = "/queue/order-update";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 向指定用户发送"新消息"通知。
     * 前端应通过 STOMP 订阅 /user/queue/new-message 接收。
     *
     * @param userId 目标用户ID
     * @param payload 要推送的消息内容
     */
    public void sendNewMessageNotification(Long userId, MessageResponse payload) {
        if (userId == null) {
            logger.warn("sendNewMessageNotification skipped: userId is null");
            return;
        }
        try {
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), USER_NEW_MESSAGE_QUEUE, payload);
        } catch (Exception e) {
            logger.error("发送 STOMP 用户消息失败, userId={}", userId, e);
        }
    }
    
    /**
     * 向卖家发送"新订单"通知。
     * 前端应通过 STOMP 订阅 /user/queue/new-order 接收。
     *
     * @param sellerId 卖家用户ID
     * @param payload 订单信息
     */
    public void sendNewOrderNotification(Long sellerId, Object payload) {
        if (sellerId == null) {
            logger.warn("sendNewOrderNotification skipped: sellerId is null");
            return;
        }
        try {
            logger.info("发送新订单通知给卖家, sellerId={}", sellerId);
            messagingTemplate.convertAndSendToUser(String.valueOf(sellerId), USER_NEW_ORDER_QUEUE, payload);
        } catch (Exception e) {
            logger.error("发送新订单通知失败, sellerId={}", sellerId, e);
        }
    }
    
    /**
     * 向用户发送"订单状态更新"通知。
     * 前端应通过 STOMP 订阅 /user/queue/order-update 接收。
     *
     * @param userId 用户ID（买家或卖家）
     * @param payload 订单更新信息
     */
    public void sendOrderUpdateNotification(Long userId, Object payload) {
        if (userId == null) {
            logger.warn("sendOrderUpdateNotification skipped: userId is null");
            return;
        }
        try {
            logger.info("发送订单更新通知, userId={}", userId);
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), USER_ORDER_UPDATE_QUEUE, payload);
        } catch (Exception e) {
            logger.error("发送订单更新通知失败, userId={}", userId, e);
        }
    }

    /**
     * 通用方法：向指定用户发送任意目的地的消息。
     */
    public void sendToUser(Long userId, String destination, Object payload) {
        if (userId == null) {
            logger.warn("sendToUser skipped: userId is null");
            return;
        }
        try {
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), destination, payload);
        } catch (Exception e) {
            logger.error("发送 STOMP 用户消息失败, userId={}, destination={}", userId, destination, e);
        }
    }
}