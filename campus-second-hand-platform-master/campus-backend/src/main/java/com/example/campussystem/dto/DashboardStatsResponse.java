package com.example.campussystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理后台仪表盘统计数据响应DTO
 */
public class DashboardStatsResponse {

    // 基础统计数据
    private Long totalUsers;
    private Long totalProducts;
    private Long totalOrders;
    private Long totalMessages;

    // 今日统计数据
    private Long todayNewUsers;
    private Long todayNewProducts;
    private Long todayNewOrders;
    private BigDecimal todayTransactionAmount;

    // 状态统计
    private Long activeUsers;
    private Long onlineUsers;
    private Long availableProducts;
    private Long soldProducts;
    private Long pendingOrders;
    private Long completedOrders;

    // 分类统计
    private List<CategoryStats> categoryStats;

    // 用户活跃度统计（最近7天）
    private List<DailyStats> userActivityStats;

    // 交易统计（最近7天）
    private List<DailyStats> transactionStats;

    // 热门商品
    private List<PopularProduct> popularProducts;

    // 最新用户
    private List<RecentUser> recentUsers;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdateTime;

    // 构造函数
    public DashboardStatsResponse() {
        this.lastUpdateTime = LocalDateTime.now();
    }

    // 内部类定义
    public static class CategoryStats {
        private String categoryName;
        private Long productCount;
        private Long orderCount;
        private BigDecimal totalAmount;

        public CategoryStats() {}

        public CategoryStats(String categoryName, Long productCount, Long orderCount, BigDecimal totalAmount) {
            this.categoryName = categoryName;
            this.productCount = productCount;
            this.orderCount = orderCount;
            this.totalAmount = totalAmount;
        }

        // Getter和Setter方法
        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public Long getProductCount() {
            return productCount;
        }

        public void setProductCount(Long productCount) {
            this.productCount = productCount;
        }

        public Long getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(Long orderCount) {
            this.orderCount = orderCount;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }

    public static class DailyStats {
        private String date;
        private Long count;
        private BigDecimal amount;

        public DailyStats() {}

        public DailyStats(String date, Long count, BigDecimal amount) {
            this.date = date;
            this.count = count;
            this.amount = amount;
        }

        // Getter和Setter方法
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    public static class PopularProduct {
        private Long id;
        private String title;
        private BigDecimal price;
        private Integer viewCount;
        private String categoryName;

        public PopularProduct() {}

        // Getter和Setter方法
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getViewCount() {
            return viewCount;
        }

        public void setViewCount(Integer viewCount) {
            this.viewCount = viewCount;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
    }

    public static class RecentUser {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private LocalDateTime createTime;

        public RecentUser() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }

    // 主要字段的Getter和Setter方法
    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(Long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public Long getTodayNewUsers() {
        return todayNewUsers;
    }

    public void setTodayNewUsers(Long todayNewUsers) {
        this.todayNewUsers = todayNewUsers;
    }

    public Long getTodayNewProducts() {
        return todayNewProducts;
    }

    public void setTodayNewProducts(Long todayNewProducts) {
        this.todayNewProducts = todayNewProducts;
    }

    public Long getTodayNewOrders() {
        return todayNewOrders;
    }

    public void setTodayNewOrders(Long todayNewOrders) {
        this.todayNewOrders = todayNewOrders;
    }

    public BigDecimal getTodayTransactionAmount() {
        return todayTransactionAmount;
    }

    public void setTodayTransactionAmount(BigDecimal todayTransactionAmount) {
        this.todayTransactionAmount = todayTransactionAmount;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Long getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(Long onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    public Long getAvailableProducts() {
        return availableProducts;
    }

    public void setAvailableProducts(Long availableProducts) {
        this.availableProducts = availableProducts;
    }

    public Long getSoldProducts() {
        return soldProducts;
    }

    public void setSoldProducts(Long soldProducts) {
        this.soldProducts = soldProducts;
    }

    public Long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public List<CategoryStats> getCategoryStats() {
        return categoryStats;
    }

    public void setCategoryStats(List<CategoryStats> categoryStats) {
        this.categoryStats = categoryStats;
    }

    public List<DailyStats> getUserActivityStats() {
        return userActivityStats;
    }

    public void setUserActivityStats(List<DailyStats> userActivityStats) {
        this.userActivityStats = userActivityStats;
    }

    public List<DailyStats> getTransactionStats() {
        return transactionStats;
    }

    public void setTransactionStats(List<DailyStats> transactionStats) {
        this.transactionStats = transactionStats;
    }

    public List<PopularProduct> getPopularProducts() {
        return popularProducts;
    }

    public void setPopularProducts(List<PopularProduct> popularProducts) {
        this.popularProducts = popularProducts;
    }

    public List<RecentUser> getRecentUsers() {
        return recentUsers;
    }

    public void setRecentUsers(List<RecentUser> recentUsers) {
        this.recentUsers = recentUsers;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
