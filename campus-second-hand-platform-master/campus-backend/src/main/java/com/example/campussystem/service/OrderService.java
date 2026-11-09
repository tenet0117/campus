package com.example.campussystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campussystem.dto.OrderRequest;
import com.example.campussystem.dto.OrderResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request, Long buyerId);

    OrderResponse getOrderDetail(Long orderId, Long userId);

    OrderResponse getOrderByOrderNo(String orderNo, Long userId);

    void cancelOrder(Long orderId, Long userId, String reason);

    void confirmOrderByBuyer(Long orderId, Long buyerId);

    void confirmOrderBySeller(Long orderId, Long sellerId);

    void completeOrder(Long orderId, Long userId);

    /**
     * 取消某商品下所有未完结订单（待确认/进行中），并将商品恢复在售。
     * 用于卖家「取消预约」等场景，避免订单与商品状态脱节。
     */
    void cancelOpenOrdersForProduct(Long productId, Long operatorId, String reason);

    /** 买卖双方查看某商品当前未完结订单（待接单/进行中） */
    OrderResponse getActiveOrderByProduct(Long productId, Long userId);

    List<OrderResponse> getBuyerOrders(Long buyerId);

    List<OrderResponse> getSellerOrders(Long sellerId);

    List<OrderResponse> getUserOrders(Long userId);

    List<OrderResponse> getUserOrdersByStatus(Long userId, Integer status);

    Page<OrderResponse> getUserOrdersPage(Long userId, int page, int size, String sortBy, String sortDir);

    List<OrderResponse> getBuyerPendingOrders(Long buyerId);

    List<OrderResponse> getSellerPendingOrders(Long sellerId);

    long countUserOrders(Long userId, String type);

    BigDecimal calculateUserTotalAmount(Long userId, String type);

    Map<String, Long> getUserOrderStats(Long userId);

    Double getUserTotalTradeAmount(Long userId);
}
