package com.example.campussystem.service;

import java.util.List;
import java.util.Map;

public interface SystemLogService {

    void logOperation(String userId, String operation, String details);

    void logLogin(String userId, String username, String ipAddress, boolean success);

    void logLogout(String userId, String username, String ipAddress);

    void logSecurityEvent(String event, String details, String ipAddress);

    void logSystemError(String error, String details);

    List<Map<String, Object>> getSystemLogs(int page, int size, String level, String keyword);

    Map<String, Object> getLogStatistics();

    void cleanOldLogs(int daysToKeep);

    String exportLogs(String startDate, String endDate, String level);
}
