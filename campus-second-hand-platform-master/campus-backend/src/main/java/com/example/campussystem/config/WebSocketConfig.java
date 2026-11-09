package com.example.campussystem.config;

import com.example.campussystem.websocket.WebSocketAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket配置类
 * 仅启用 STOMP 协议的消息代理与端点
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Value("${websocket.allowed-origins:*}")
    private String allowedOrigins;

    /**
     * STOMP协议配置 - 用于结构化消息传递
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的基于内存的消息代理，将消息返回给前缀为"/topic"和"/queue"的目的地
        config.enableSimpleBroker("/topic", "/queue");
        
        // 设置应用程序目的地前缀
        config.setApplicationDestinationPrefixes("/app");
        
        // 设置用户目的地前缀
        config.setUserDestinationPrefix("/user");
    }

    /**
     * STOMP端点注册 - 用于客户端连接
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点，支持原生WebSocket连接
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins)
                .addInterceptors(webSocketAuthInterceptor);
        
        // 同时保留SockJS支持作为回退选项
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns(allowedOrigins)
                .addInterceptors(webSocketAuthInterceptor)
                .withSockJS();
    }

}