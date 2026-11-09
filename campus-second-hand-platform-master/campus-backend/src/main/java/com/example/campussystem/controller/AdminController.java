package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.dto.ProductResponse;
import com.example.campussystem.dto.UserProfileResponse;
import com.example.campussystem.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员控制器
 * 提供管理后台相关的API接口
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取系统统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStatistics() {
        Map<String, Object> statistics = adminService.getSystemStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics, "获取系统统计信息成功"));
    }

    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserProfileResponse>>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword) {
        
        Page<UserProfileResponse> users = adminService.getUserList(page, size, sortBy, sortDir, keyword);
        return ResponseEntity.ok(ApiResponse.success(users, "获取用户列表成功"));
    }

    /**
     * 禁用/启用用户
     */
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        
        adminService.updateUserStatus(userId, status);
        String message = status == 1 ? "用户启用成功" : "用户禁用成功";
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * 获取商品列表（分页，包含已删除）
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        
        Page<ProductResponse> products = adminService.getProductList(page, size, sortBy, sortDir, keyword, status);
        return ResponseEntity.ok(ApiResponse.success(products, "获取商品列表成功"));
    }

    /**
     * 审核商品（上架/下架）
     */
    @PatchMapping("/products/{productId}/status")
    public ResponseEntity<ApiResponse<Void>> updateProductStatus(
            @PathVariable Long productId,
            @RequestParam Integer status) {
        
        adminService.updateProductStatus(productId, status);
        String message = getProductStatusMessage(status);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * 强制删除商品
     */
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<Void>> forceDeleteProduct(@PathVariable Long productId) {
        adminService.forceDeleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("商品删除成功"));
    }

    /**
     * 获取用户详细信息
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserDetail(@PathVariable Long userId) {
        Map<String, Object> userDetail = adminService.getUserDetail(userId);
        return ResponseEntity.ok(ApiResponse.success(userDetail, "获取用户详情成功"));
    }

    /**
     * 获取商品详细信息（管理员视图）
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductDetail(@PathVariable Long productId) {
        Map<String, Object> productDetail = adminService.getProductDetail(productId);
        return ResponseEntity.ok(ApiResponse.success(productDetail, "获取商品详情成功"));
    }

    /**
     * 获取用户活跃度统计
     */
    @GetMapping("/statistics/user-activity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserActivityStatistics(
            @RequestParam(defaultValue = "7") int days) {
        
        Map<String, Object> statistics = adminService.getUserActivityStatistics(days);
        return ResponseEntity.ok(ApiResponse.success(statistics, "获取用户活跃度统计成功"));
    }

    /**
     * 获取商品分类统计
     */
    @GetMapping("/statistics/category")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCategoryStatistics() {
        Map<String, Object> statistics = adminService.getCategoryStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics, "获取分类统计成功"));
    }

    /**
     * 获取交易统计
     */
    @GetMapping("/statistics/trade")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTradeStatistics(
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> statistics = adminService.getTradeStatistics(days);
        return ResponseEntity.ok(ApiResponse.success(statistics, "获取交易统计成功"));
    }

    /**
     * 批量操作用户状态
     */
    @PatchMapping("/users/batch-status")
    public ResponseEntity<ApiResponse<Void>> batchUpdateUserStatus(
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        java.util.List<Long> userIds = (java.util.List<Long>) request.get("userIds");
        Integer status = (Integer) request.get("status");
        
        adminService.batchUpdateUserStatus(userIds, status);
        String message = status == 1 ? "批量启用用户成功" : "批量禁用用户成功";
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * 批量操作商品状态
     */
    @PatchMapping("/products/batch-status")
    public ResponseEntity<ApiResponse<Void>> batchUpdateProductStatus(
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        java.util.List<Long> productIds = (java.util.List<Long>) request.get("productIds");
        Integer status = (Integer) request.get("status");
        
        adminService.batchUpdateProductStatus(productIds, status);
        String message = getProductStatusMessage(status);
        return ResponseEntity.ok(ApiResponse.success("批量" + message));
    }

    /**
     * 删除管理员
     */
    // @DeleteMapping("/{adminId}") // 注释掉以避免与AdminAuthController冲突
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable Long adminId) {
        adminService.deleteAdmin(adminId);
        return ResponseEntity.ok(ApiResponse.success("管理员删除成功"));
    }

    /**
     * 获取商品状态对应的消息
     */
    private String getProductStatusMessage(Integer status) {
        switch (status) {
            case 0: return "商品下架成功";
            case 1: return "商品上架成功";
            case 2: return "商品待审核";
            case 3: return "商品已售出";
            default: return "商品状态更新成功";
        }
    }
}