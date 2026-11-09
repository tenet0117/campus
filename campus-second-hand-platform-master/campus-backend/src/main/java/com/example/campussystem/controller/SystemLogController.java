package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统日志控制器
 */
@RestController
@RequestMapping("/system/logs")
@PreAuthorize("hasRole('ADMIN')")
public class SystemLogController {

    @Autowired
    private SystemLogService systemLogService;

    /**
     * 获取系统日志列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSystemLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword) {
        
        List<Map<String, Object>> logs = systemLogService.getSystemLogs(page, size, level, keyword);
        return ResponseEntity.ok(ApiResponse.success(logs, "获取系统日志成功"));
    }

    /**
     * 获取日志统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLogStatistics() {
        Map<String, Object> statistics = systemLogService.getLogStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics, "获取日志统计信息成功"));
    }

    /**
     * 清理旧日志
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<ApiResponse<Void>> cleanOldLogs(
            @RequestParam(defaultValue = "30") int daysToKeep) {
        
        systemLogService.cleanOldLogs(daysToKeep);
        return ResponseEntity.ok(ApiResponse.success("日志清理完成"));
    }

    /**
     * 导出日志
     */
    @PostMapping("/export")
    public ResponseEntity<ApiResponse<String>> exportLogs(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String level) {
        
        String exportPath = systemLogService.exportLogs(startDate, endDate, level);
        return ResponseEntity.ok(ApiResponse.success(exportPath, "日志导出成功"));
    }

    /**
     * 记录操作日志
     */
    @PostMapping("/operation")
    public ResponseEntity<ApiResponse<Void>> logOperation(
            @RequestParam String userId,
            @RequestParam String operation,
            @RequestParam String details) {
        
        systemLogService.logOperation(userId, operation, details);
        return ResponseEntity.ok(ApiResponse.success("操作日志记录成功"));
    }

    /**
     * 记录安全事件
     */
    @PostMapping("/security")
    public ResponseEntity<ApiResponse<Void>> logSecurityEvent(
            @RequestParam String event,
            @RequestParam String details,
            @RequestParam String ipAddress) {
        
        systemLogService.logSecurityEvent(event, details, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("安全事件记录成功"));
    }
}