package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.dto.ProductResponse;
import com.example.campussystem.dto.UserProfileResponse;
import com.example.campussystem.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;

/**
 * 管理后台业务管理控制器
 */
@RestController
@RequestMapping("/admin/management")
@Tag(name = "管理后台业务管理", description = "用户管理、商品管理、订单管理等接口")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminManagementController {

    @Autowired
    private AdminService adminService;

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    @Operation(summary = "获取用户列表", description = "分页获取用户列表，支持搜索和排序")
    public ApiResponse<Page<UserProfileResponse>> getUserList(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword) {
        Page<UserProfileResponse> result = adminService.getUserList(page, size, sortBy, sortDir, keyword);
        return ApiResponse.success(result);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "获取用户详情", description = "获取指定用户的详细信息和统计数据")
    public ApiResponse<Map<String, Object>> getUserDetail(@PathVariable Long userId) {
        Map<String, Object> result = adminService.getUserDetail(userId);
        return ApiResponse.success(result);
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "更新用户状态", description = "启用或禁用用户账户")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long userId,
                                             @RequestParam Integer status) {
        adminService.updateUserStatus(userId, status);
        return ApiResponse.success(null, "用户状态更新成功");
    }

    @PutMapping("/users/batch/status")
    @Operation(summary = "批量更新用户状态", description = "批量启用或禁用用户账户")
    public ApiResponse<Void> batchUpdateUserStatus(@RequestBody List<Long> userIds,
                                                  @RequestParam Integer status) {
        adminService.batchUpdateUserStatus(userIds, status);
        return ApiResponse.success(null, "批量更新用户状态成功");
    }

    // ==================== 商品管理 ====================

    @GetMapping("/products")
    @Operation(summary = "获取商品列表", description = "分页获取商品列表，支持搜索和状态筛选")
    public ApiResponse<Page<ProductResponse>> getProductList(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<ProductResponse> result = adminService.getProductList(page, size, sortBy, sortDir, keyword, status);
        return ApiResponse.success(result);
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "获取商品详情", description = "获取指定商品的详细信息和统计数据")
    public ApiResponse<Map<String, Object>> getProductDetail(@PathVariable Long productId) {
        Map<String, Object> result = adminService.getProductDetail(productId);
        return ApiResponse.success(result);
    }

    @PutMapping("/products/{productId}/status")
    @Operation(summary = "更新商品状态", description = "更新商品状态（上架、下架、已售出等）")
    public ApiResponse<Void> updateProductStatus(@PathVariable Long productId,
                                                @RequestParam Integer status) {
        adminService.updateProductStatus(productId, status);
        return ApiResponse.success(null, "商品状态更新成功");
    }

    @DeleteMapping("/products/{productId}")
    @Operation(summary = "强制删除商品", description = "强制删除商品（仅限管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> forceDeleteProduct(@PathVariable Long productId) {
        adminService.forceDeleteProduct(productId);
        return ApiResponse.success(null, "商品删除成功");
    }

    @PutMapping("/products/batch/status")
    @Operation(summary = "批量更新商品状态", description = "批量更新商品状态")
    public ApiResponse<Void> batchUpdateProductStatus(@RequestBody List<Long> productIds,
                                                     @RequestParam Integer status) {
        adminService.batchUpdateProductStatus(productIds, status);
        return ApiResponse.success(null, "批量更新商品状态成功");
    }

    // ==================== 订单管理 ====================

    @GetMapping("/orders")
    @Operation(summary = "获取订单列表", description = "分页获取订单列表，支持搜索和状态筛选")
    public ApiResponse<Page<com.example.campussystem.dto.OrderResponse>> getOrderList(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<com.example.campussystem.dto.OrderResponse> result = adminService.getOrderList(page, size, sortBy, sortDir, keyword, status);
        return ApiResponse.success(result);
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "获取订单详情", description = "获取指定订单的详细信息")
    public ApiResponse<Map<String, Object>> getOrderDetail(@PathVariable Long orderId) {
        Map<String, Object> result = adminService.getOrderDetail(orderId);
        return ApiResponse.success(result);
    }

    @PutMapping("/orders/{orderId}/status")
    @Operation(summary = "更新订单状态", description = "更新订单状态（待确认、进行中、已完成、已取消）")
    public ApiResponse<Void> updateOrderStatus(@PathVariable Long orderId,
                                              @RequestParam Integer status) {
        adminService.updateOrderStatus(orderId, status);
        return ApiResponse.success(null, "订单状态更新成功");
    }

    @DeleteMapping("/orders/{orderId}")
    @Operation(summary = "删除订单", description = "删除订单（仅限管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteOrder(@PathVariable Long orderId) {
        adminService.deleteOrder(orderId);
        return ApiResponse.success(null, "订单删除成功");
    }

    // ==================== 统计分析 ====================

    @GetMapping("/statistics/system")
    @Operation(summary = "获取系统统计信息", description = "获取系统整体统计数据")
    public ApiResponse<Map<String, Object>> getSystemStatistics() {
        Map<String, Object> result = adminService.getSystemStatistics();
        return ApiResponse.success(result);
    }

    @GetMapping("/statistics/users")
    @Operation(summary = "获取用户活跃度统计", description = "获取指定天数内的用户活跃度统计")
    public ApiResponse<Map<String, Object>> getUserActivityStatistics(
            @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> result = adminService.getUserActivityStatistics(days);
        return ApiResponse.success(result);
    }

    @GetMapping("/statistics/categories")
    @Operation(summary = "获取分类统计", description = "获取各分类的商品数量统计")
    public ApiResponse<Map<String, Object>> getCategoryStatistics() {
        Map<String, Object> result = adminService.getCategoryStatistics();
        return ApiResponse.success(result);
    }

    @GetMapping("/statistics/trades")
    @Operation(summary = "获取交易统计", description = "获取指定天数内的交易统计数据")
    public ApiResponse<Map<String, Object>> getTradeStatistics(
            @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> result = adminService.getTradeStatistics(days);
        return ApiResponse.success(result);
    }

    // ==================== 系统管理 ====================

    @GetMapping("/logs")
    @Operation(summary = "获取系统日志", description = "分页获取系统日志")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<Map<String, Object>>> getSystemLogs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword) {
        Page<Map<String, Object>> result = adminService.getSystemLogs(page, size, level, keyword);
        return ApiResponse.success(result);
    }

    @PostMapping("/cleanup")
    @Operation(summary = "清理系统数据", description = "清理指定天数前的已删除数据")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> cleanupSystemData(@RequestParam(defaultValue = "30") int days) {
        adminService.cleanupSystemData(days);
        return ApiResponse.success(null, "系统数据清理完成");
    }
}
