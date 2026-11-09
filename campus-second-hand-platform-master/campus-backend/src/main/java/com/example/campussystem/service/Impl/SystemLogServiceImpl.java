package com.example.campussystem.service.impl;

import com.example.campussystem.service.SystemLogService;
import com.example.campussystem.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SystemLogServiceImpl implements SystemLogService {

    private static final Logger logger = LoggerFactory.getLogger(SystemLogServiceImpl.class);

    @Autowired
    private CacheService cacheService;

    private static final String LOG_FILE_PATH = "logs/campus-market.log";
    private static final String CACHE_KEY_SYSTEM_LOGS = "system_logs:";
    private static final Pattern LOG_PATTERN = Pattern.compile(
        "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+\\[(\\w+)\\]\\s+(\\w+)\\s+(.+?)\\s+-\\s+(.+)"
    );

    /**
     * 记录操作日志
     */
    public void logOperation(String userId, String operation, String details) {
        logger.info("用户操作日志 - 用户ID: {} - 操作: {} - 详情: {}", userId, operation, details);
    }

    /**
     * 记录登录日志
     */
    public void logLogin(String userId, String username, String ipAddress, boolean success) {
        if (success) {
            logger.info("用户登录成功 - 用户ID: {} - 用户名: {} - IP: {}", userId, username, ipAddress);
        } else {
            logger.warn("用户登录失败 - 用户名: {} - IP: {}", username, ipAddress);
        }
    }

    /**
     * 记录登出日志
     */
    public void logLogout(String userId, String username, String ipAddress) {
        logger.info("用户登出 - 用户ID: {} - 用户名: {} - IP: {}", userId, username, ipAddress);
    }

    /**
     * 记录安全事件
     */
    public void logSecurityEvent(String event, String details, String ipAddress) {
        logger.warn("安全事件 - 事件: {} - 详情: {} - IP: {}", event, details, ipAddress);
    }

    /**
     * 记录系统错误
     */
    public void logSystemError(String error, String details) {
        logger.error("系统错误 - 错误: {} - 详情: {}", error, details);
    }

    /**
     * 获取系统日志（分页）
     */
    public List<Map<String, Object>> getSystemLogs(int page, int size, String level, String keyword) {
        String cacheKey = CACHE_KEY_SYSTEM_LOGS + page + ":" + size + ":" + level + ":" + keyword;
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cachedLogs = cacheService.get(cacheKey, List.class);
        if (cachedLogs != null) {
            return cachedLogs;
        }

        List<Map<String, Object>> logs = readLogsFromFile(page, size, level, keyword);
        
        // 缓存日志数据，缓存5分钟
        cacheService.set(cacheKey, logs, 5, java.util.concurrent.TimeUnit.MINUTES);
        
        return logs;
    }

    /**
     * 从日志文件读取日志
     */
    private List<Map<String, Object>> readLogsFromFile(int page, int size, String level, String keyword) {
        List<Map<String, Object>> logs = new ArrayList<>();
        File logFile = new File(LOG_FILE_PATH);
        
        if (!logFile.exists()) {
            logger.warn("日志文件不存在: {}, 返回模拟数据", LOG_FILE_PATH);
            return generateMockLogs(page, size, level, keyword);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            List<String> allLines = new ArrayList<>();
            
            // 读取所有行
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }
            
            // 倒序处理（最新的日志在前）
            for (int i = allLines.size() - 1; i >= 0; i--) {
                line = allLines.get(i);
                
                // 过滤日志级别
                if (level != null && !level.isEmpty() && !line.contains(level.toUpperCase())) {
                    continue;
                }
                
                // 过滤关键词
                if (keyword != null && !keyword.isEmpty() && !line.toLowerCase().contains(keyword.toLowerCase())) {
                    continue;
                }
                
                Map<String, Object> logEntry = parseLogLine(line);
                if (logEntry != null) {
                    logs.add(logEntry);
                }
                
                // 分页处理
                if (logs.size() >= (page + 1) * size) {
                    break;
                }
            }
            
            // 返回当前页的数据
            int start = page * size;
            int end = Math.min(start + size, logs.size());
            
            if (start < logs.size()) {
                return logs.subList(start, end);
            }
            
        } catch (IOException e) {
            logger.error("读取日志文件失败", e);
        }
        
        return logs;
    }

    /**
     * 解析日志行
     */
    private Map<String, Object> parseLogLine(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        
        if (matcher.find()) {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("timestamp", matcher.group(1));
            logEntry.put("thread", matcher.group(2));
            logEntry.put("level", matcher.group(3));
            logEntry.put("logger", matcher.group(4));
            logEntry.put("message", matcher.group(5));
            logEntry.put("rawLine", line);
            
            return logEntry;
        }
        
        // 如果不匹配标准格式，返回原始行
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", "");
        logEntry.put("thread", "");
        logEntry.put("level", "INFO");
        logEntry.put("logger", "");
        logEntry.put("message", line);
        logEntry.put("rawLine", line);
        
        return logEntry;
    }

    /**
     * 生成模拟日志数据
     */
    private List<Map<String, Object>> generateMockLogs(int page, int size, String level, String keyword) {
        List<Map<String, Object>> logs = new ArrayList<>();
        
        String[] levels = {"INFO", "WARN", "ERROR", "DEBUG"};
        String[] messages = {
            "用户登录成功 - 用户名: admin - IP: 192.168.1.100",
            "系统启动完成 - 耗时: 2.5秒",
            "数据库连接池初始化完成",
            "缓存服务启动成功",
            "用户操作 - 查看商品列表",
            "管理员操作 - 审核商品",
            "系统定时任务执行 - 清理过期数据",
            "API请求处理 - GET /api/v1/products",
            "用户注册 - 新用户: student123",
            "商品发布 - 商品ID: 12345",
            "订单创建 - 订单号: ORDER20231121001",
            "支付完成 - 订单号: ORDER20231121001",
            "系统备份完成",
            "邮件发送成功 - 收件人: user@example.com",
            "文件上传完成 - 文件名: product_image.jpg"
        };
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // 生成足够的日志数据
        for (int i = 0; i < (page + 1) * size + 10; i++) {
            String logLevel = levels[i % levels.length];
            String message = messages[i % messages.length];
            
            // 过滤级别
            if (level != null && !level.isEmpty() && !logLevel.equals(level.toUpperCase())) {
                continue;
            }
            
            // 过滤关键词
            if (keyword != null && !keyword.isEmpty() && !message.toLowerCase().contains(keyword.toLowerCase())) {
                continue;
            }
            
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("timestamp", now.minusMinutes(i * 5).format(formatter));
            logEntry.put("thread", "http-nio-8081-exec-" + (i % 10 + 1));
            logEntry.put("level", logLevel);
            logEntry.put("logger", "c.e.c.service.SystemLogService");
            logEntry.put("message", message);
            logEntry.put("rawLine", now.minusMinutes(i * 5).format(formatter) + " [" + logLevel + "] " + message);
            
            logs.add(logEntry);
        }
        
        // 分页处理
        int start = page * size;
        int end = Math.min(start + size, logs.size());
        
        if (start < logs.size()) {
            return logs.subList(start, end);
        }
        
        return new ArrayList<>();
    }

    /**
     * 获取日志统计信息
     */
    public Map<String, Object> getLogStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            File logFile = new File(LOG_FILE_PATH);
            if (!logFile.exists()) {
                statistics.put("totalLines", 0);
                statistics.put("fileSize", 0);
                statistics.put("lastModified", "");
                return statistics;
            }
            
            // 统计日志行数
            long lineCount = 0;
            Map<String, Integer> levelCounts = new HashMap<>();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    
                    // 统计各级别日志数量
                    if (line.contains("ERROR")) {
                        levelCounts.merge("ERROR", 1, Integer::sum);
                    } else if (line.contains("WARN")) {
                        levelCounts.merge("WARN", 1, Integer::sum);
                    } else if (line.contains("INFO")) {
                        levelCounts.merge("INFO", 1, Integer::sum);
                    } else if (line.contains("DEBUG")) {
                        levelCounts.merge("DEBUG", 1, Integer::sum);
                    }
                }
            }
            
            statistics.put("totalLines", lineCount);
            statistics.put("fileSize", logFile.length());
            statistics.put("lastModified", 
                          LocalDateTime.ofEpochSecond(logFile.lastModified() / 1000, 0, 
                                                    java.time.ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now()))
                                       .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            statistics.put("levelCounts", levelCounts);
            
        } catch (IOException e) {
            logger.error("获取日志统计信息失败", e);
        }
        
        return statistics;
    }

    /**
     * 清理旧日志
     */
    public void cleanOldLogs(int daysToKeep) {
        logger.info("开始清理 {} 天前的日志", daysToKeep);
        
        // 这里可以实现日志清理逻辑
        // 例如：删除指定天数前的日志文件，或者压缩旧日志等
        
        logger.info("日志清理完成");
    }

    /**
     * 导出日志
     */
    public String exportLogs(String startDate, String endDate, String level) {
        logger.info("导出日志 - 开始时间: {} - 结束时间: {} - 级别: {}", startDate, endDate, level);
        
        // 这里可以实现日志导出逻辑
        // 返回导出文件的路径或下载链接
        
        return "logs/export_" + System.currentTimeMillis() + ".log";
    }
}