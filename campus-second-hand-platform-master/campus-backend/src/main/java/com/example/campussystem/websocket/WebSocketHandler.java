package com.example.campussystem.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 原生 WebSocket 处理器已禁用，后端仅保留 STOMP。
 * 该类保留为最小占位实现以避免依赖和编译错误。
 */
public class WebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    // 占位实现，无任何业务逻辑。
}