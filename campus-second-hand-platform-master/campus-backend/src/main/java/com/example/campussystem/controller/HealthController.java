package com.example.campussystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供应用程序健康状态检查接口
 */
@RestController
public class HealthController {

    /**
     * 健康检查端点
     * @return 应用程序健康状态信息
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("application", "Campus Trading System");
        healthInfo.put("version", "1.0.0");
        
        return ResponseEntity.ok(healthInfo);
    }

    /**
     * Actuator风格的健康检查端点
     * @return 简化的健康状态信息
     */
    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, String>> actuatorHealth() {
        Map<String, String> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        
        return ResponseEntity.ok(healthInfo);
    }
}