package com.example.campussystem.websocket;

import com.example.campussystem.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * WebSocket 握手拦截器：必须携带有效 JWT，否则拒绝握手
 */
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            String authHeader = request.getHeaders().getFirst("Authorization");
            String token = jwtUtil.getTokenFromHeader(authHeader);

            if (token == null || token.isEmpty()) {
                token = extractTokenFromQuery(request.getURI());
            }

            if (token == null || !jwtUtil.validateToken(token)) {
                logger.warn("WebSocket握手失败：未提供有效token - {}", request.getURI());
                return false;
            }

            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            attributes.put("username", username);
            attributes.put("userId", userId);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                String.valueOf(userId),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (role == null ? "USER" : role)))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.info("WebSocket握手认证成功，用户: {}, userId: {}", username, userId);
            return true;
        } catch (Exception e) {
            logger.error("WebSocket握手认证失败", e);
            return false;
        }
    }

    private String extractTokenFromQuery(URI uri) {
        try {
            String query = uri.getQuery();
            if (query == null) {
                return null;
            }
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2 && "token".equalsIgnoreCase(kv[0])) {
                    return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            logger.warn("解析查询参数获取token失败", e);
        }
        return null;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            logger.error("WebSocket握手后发生错误", exception);
        }
    }
}
