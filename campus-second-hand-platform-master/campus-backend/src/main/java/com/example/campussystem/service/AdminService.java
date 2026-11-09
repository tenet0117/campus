package com.example.campussystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campussystem.dto.OrderResponse;
import com.example.campussystem.dto.ProductResponse;
import com.example.campussystem.dto.UserProfileResponse;

import java.util.List;
import java.util.Map;

public interface AdminService {

    Map<String, Object> getSystemStatistics();

    Page<UserProfileResponse> getUserList(int pageNum, int size, String sortBy, String sortDir, String keyword);

    void updateUserStatus(Long userId, Integer status);

    Page<ProductResponse> getProductList(int pageNum, int size, String sortBy, String sortDir, String keyword, Integer status);

    void updateProductStatus(Long productId, Integer status);

    void forceDeleteProduct(Long productId);

    Page<OrderResponse> getOrderList(int pageNum, int size, String sortBy, String sortDir, String keyword, Integer status);

    Map<String, Object> getOrderDetail(Long orderId);

    void updateOrderStatus(Long orderId, Integer status);

    void deleteOrder(Long orderId);

    Map<String, Object> getUserDetail(Long userId);

    Map<String, Object> getProductDetail(Long productId);

    void batchUpdateUserStatus(List<Long> userIds, Integer status);

    void batchUpdateProductStatus(List<Long> productIds, Integer status);

    Map<String, Object> getUserActivityStatistics(int days);

    Map<String, Object> getCategoryStatistics();

    Map<String, Object> getTradeStatistics(int days);

    Page<Map<String, Object>> getSystemLogs(int pageNum, int size, String level, String keyword);

    void cleanupSystemData(int days);

    void deleteAdmin(Long adminId);
}
