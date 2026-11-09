package com.example.campussystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campussystem.dto.OrderRequest;
import com.example.campussystem.dto.OrderResponse;
import com.example.campussystem.entity.Order;
import com.example.campussystem.entity.Product;
import com.example.campussystem.entity.User;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.mapper.OrderMapper;
import com.example.campussystem.mapper.ProductMapper;
import com.example.campussystem.mapper.UserMapper;
import com.example.campussystem.service.OrderService;
import com.example.campussystem.service.CacheService;
import com.example.campussystem.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private CacheService cacheService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long buyerId) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (product.getUserId().equals(buyerId)) {
            throw new BusinessException("不能购买自己发布的商品");
        }

        BigDecimal actualAmount = product.getPrice();
        if (actualAmount == null || actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("商品价格无效");
        }

        Long openCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getProductId, request.getProductId())
                .in(Order::getStatus, 1, 2));
        if (openCount != null && openCount > 0) {
            throw new BusinessException(409, "商品已有进行中的订单，暂不可购买");
        }

        validateReceiverInfo(request);

        // 条件更新：仅当商品仍为在售(status=1)时改为已预订，防止并发超卖
        int updated = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, request.getProductId())
                .eq(Product::getStatus, 1)
                .set(Product::getStatus, 2));
        if (updated == 0) {
            throw new BusinessException(409, "商品已被预订或不可购买");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getUserId());
        order.setProductId(request.getProductId());
        order.setAmount(actualAmount);
        order.setTradeType(request.getTradeType() == null ? 1 : request.getTradeType());
        order.setReceiverName(trimToNull(request.getReceiverName()));
        order.setReceiverPhone(trimToNull(request.getReceiverPhone()));
        order.setCampus(trimToNull(request.getCampus()));
        order.setBuilding(trimToNull(request.getBuilding()));
        order.setAddressDetail(trimToNull(request.getAddressDetail()));
        order.setRemark(trimToNull(request.getRemark()));
        order.setStatus(1);
        order.setBuyerConfirm(0);
        order.setSellerConfirm(0);
        orderMapper.insert(order);

        OrderResponse response = convertToResponse(order);
        Long sellerId = product.getUserId();
        runAfterCommit(() -> {
            try {
                webSocketService.sendNewOrderNotification(sellerId, response);
            } catch (Exception e) {
                logger.warn("发送新订单通知失败: {}", e.getMessage());
            }
        });
        return response;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限查看此订单");
        }

        return convertToResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNo(String orderNo, Long userId) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) throw new BusinessException("订单不存在");

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限查看此订单");
        }

        return convertToResponse(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限操作此订单");
        }

        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException("订单状态不允许取消");
        }

        order.setStatus(4);
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(order);

        restoreProductIfNoOpenOrders(order.getProductId());

        clearUserStatsCache(order.getBuyerId());
        clearUserStatsCache(order.getSellerId());

        try {
            OrderResponse response = convertToResponse(order);
            runAfterCommit(() -> {
                try {
                    webSocketService.sendOrderUpdateNotification(order.getBuyerId(), response);
                    webSocketService.sendOrderUpdateNotification(order.getSellerId(), response);
                } catch (Exception e) {
                    logger.warn("发送订单取消通知失败: {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.warn("准备订单取消通知失败: {}", e.getMessage());
        }
    }

    /**
     * 流程：
     * - status=1：买家不可接单，需等待卖家
     * - status=2：买家确认当面交割完成；若卖家也已确认则订单完成
     */
    @Transactional
    public void confirmOrderByBuyer(Long orderId, Long buyerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getBuyerId().equals(buyerId)) throw new BusinessException("无权限操作此订单");
        if (order.getStatus() == 3) throw new BusinessException("订单已完成");
        if (order.getStatus() == 4) throw new BusinessException("订单已取消，无法确认");
        if (order.getStatus() == 1) throw new BusinessException("请等待卖家确认接单");
        if (order.getStatus() != 2) throw new BusinessException("当前订单状态不可确认完成");

        order.setBuyerConfirm(1);
        if (Integer.valueOf(1).equals(order.getSellerConfirm())) {
            markOrderCompleted(order);
        } else {
            orderMapper.updateById(order);
            notifyOrderUpdate(order);
        }
        clearUserStatsCache(order.getBuyerId());
        clearUserStatsCache(order.getSellerId());
    }

    /**
     * 流程：
     * - status=1：卖家接单 → 进行中（确认标记清零，留给双方交割确认）
     * - status=2：卖家确认当面交割完成；若买家也已确认则订单完成
     */
    @Transactional
    public void confirmOrderBySeller(Long orderId, Long sellerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getSellerId().equals(sellerId)) throw new BusinessException("无权限操作此订单");
        if (order.getStatus() == 3) throw new BusinessException("订单已完成");
        if (order.getStatus() == 4) throw new BusinessException("订单已取消，无法确认");

        if (order.getStatus() == 1) {
            order.setStatus(2);
            order.setBuyerConfirm(0);
            order.setSellerConfirm(0);
            orderMapper.updateById(order);
            clearUserStatsCache(order.getBuyerId());
            clearUserStatsCache(order.getSellerId());
            notifyOrderUpdate(order);
            return;
        }

        if (order.getStatus() != 2) throw new BusinessException("当前订单状态不可确认完成");

        order.setSellerConfirm(1);
        if (Integer.valueOf(1).equals(order.getBuyerConfirm())) {
            markOrderCompleted(order);
        } else {
            orderMapper.updateById(order);
            notifyOrderUpdate(order);
        }
        clearUserStatsCache(order.getBuyerId());
        clearUserStatsCache(order.getSellerId());
    }

    /**
     * 仅允许在双方均已确认交割后幂等完成；禁止单方直接完结绕过确认。
     */
    @Transactional
    public void completeOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限操作此订单");
        }

        if (order.getStatus() == 3) return;
        if (order.getStatus() == 4) throw new BusinessException("订单已取消，无法完成");
        if (order.getStatus() != 2) throw new BusinessException("请先由卖家确认接单");
        if (!Integer.valueOf(1).equals(order.getBuyerConfirm())
                || !Integer.valueOf(1).equals(order.getSellerConfirm())) {
            throw new BusinessException("需买卖双方都确认完成后才能结束订单");
        }

        markOrderCompleted(order);
        clearUserStatsCache(order.getBuyerId());
        clearUserStatsCache(order.getSellerId());
    }

    @Transactional
    public void cancelOpenOrdersForProduct(Long productId, Long operatorId, String reason) {
        List<Order> openOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getProductId, productId)
                .in(Order::getStatus, 1, 2));
        if (openOrders.isEmpty()) {
            Product product = productMapper.selectById(productId);
            if (product != null && Integer.valueOf(2).equals(product.getStatus())) {
                product.setStatus(1);
                productMapper.updateById(product);
            }
            return;
        }

        String cancelReason = (reason == null || reason.isBlank()) ? "卖家取消预约" : reason;
        for (Order order : openOrders) {
            if (operatorId != null
                    && !order.getBuyerId().equals(operatorId)
                    && !order.getSellerId().equals(operatorId)) {
                throw new BusinessException("无权限取消该商品相关订单");
            }
            order.setStatus(4);
            order.setCancelReason(cancelReason);
            order.setCancelTime(LocalDateTime.now());
            orderMapper.updateById(order);
            clearUserStatsCache(order.getBuyerId());
            clearUserStatsCache(order.getSellerId());
            notifyOrderUpdate(order);
        }

        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setStatus(1);
            productMapper.updateById(product);
        }
    }

    private void markOrderCompleted(Order order) {
        order.setBuyerConfirm(1);
        order.setSellerConfirm(1);
        order.setStatus(3);
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);

        Product product = productMapper.selectById(order.getProductId());
        if (product != null && !Integer.valueOf(3).equals(product.getStatus())) {
            product.setStatus(3);
            productMapper.updateById(product);
        }
        notifyOrderUpdate(order);
    }

    private void restoreProductIfNoOpenOrders(Long productId) {
        Long open = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getProductId, productId)
                .in(Order::getStatus, 1, 2));
        if (open != null && open > 0) return;

        Product product = productMapper.selectById(productId);
        if (product != null && Integer.valueOf(2).equals(product.getStatus())) {
            product.setStatus(1);
            productMapper.updateById(product);
        }
    }

    private void notifyOrderUpdate(Order order) {
        try {
            OrderResponse response = convertToResponse(order);
            runAfterCommit(() -> {
                try {
                    webSocketService.sendOrderUpdateNotification(order.getBuyerId(), response);
                    webSocketService.sendOrderUpdateNotification(order.getSellerId(), response);
                } catch (Exception e) {
                    logger.warn("发送订单更新通知失败: {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.warn("准备订单更新通知失败: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public OrderResponse getActiveOrderByProduct(Long productId, Long userId) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getProductId, productId)
                .in(Order::getStatus, 1, 2)
                .and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId))
                .orderByDesc(Order::getCreateTime)
                .last("LIMIT 1"));
        if (order == null) {
            return null;
        }
        return convertToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getBuyerOrders(Long buyerId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getBuyerId, buyerId)
                .orderByDesc(Order::getCreateTime));
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getSellerOrders(Long sellerId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getSellerId, sellerId)
                .orderByDesc(Order::getCreateTime));
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId))
                .orderByDesc(Order::getCreateTime));
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrdersByStatus(Long userId, Integer status) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId))
                .eq(Order::getStatus, status)
                .orderByDesc(Order::getCreateTime));
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrdersPage(Long userId, int page, int size, String sortBy, String sortDir) {
        Page<Order> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId));

        if ("ASC".equalsIgnoreCase(sortDir)) {
            wrapper.orderByAsc(Order::getCreateTime);
        } else {
            wrapper.orderByDesc(Order::getCreateTime);
        }

        Page<Order> orderPage = orderMapper.selectPage(mpPage, wrapper);
        Page<OrderResponse> result = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        result.setRecords(orderPage.getRecords().stream().map(this::convertToResponse).collect(Collectors.toList()));
        return result;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getBuyerPendingOrders(Long buyerId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getBuyerId, buyerId)
                .eq(Order::getBuyerConfirm, 0)
                .in(Order::getStatus, Arrays.asList(1, 2)));
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getSellerPendingOrders(Long sellerId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getSellerId, sellerId)
                .eq(Order::getSellerConfirm, 0)
                .in(Order::getStatus, Arrays.asList(1, 2)));
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countUserOrders(Long userId, String type) {
        switch (type.toLowerCase()) {
            case "buyer":
                return orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getBuyerId, userId));
            case "seller":
                return orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getSellerId, userId));
            default:
                return orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                        .and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId)));
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateUserTotalAmount(Long userId, String type) {
        switch (type.toLowerCase()) {
            case "buyer":
                return sumAmountByBuyer(userId);
            case "seller":
                return sumAmountBySeller(userId);
            default:
                BigDecimal buyerAmount = sumAmountByBuyer(userId);
                BigDecimal sellerAmount = sumAmountBySeller(userId);
                return buyerAmount.add(sellerAmount);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getUserOrderStats(Long userId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 1).and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId))));
        stats.put("processing", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 2).and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId))));
        stats.put("completed", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 3).and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId))));
        stats.put("cancelled", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 4).and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId))));

        stats.put("asBuyer", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getBuyerId, userId)));
        stats.put("asSeller", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getSellerId, userId)));
        stats.put("total", stats.get("asBuyer") + stats.get("asSeller"));

        return stats;
    }

    @Transactional(readOnly = true)
    public Double getUserTotalTradeAmount(Long userId) {
        try {
            BigDecimal buyerAmount = sumCompletedAmountByBuyer(userId);
            BigDecimal sellerAmount = sumCompletedAmountBySeller(userId);

            if (buyerAmount == null) buyerAmount = BigDecimal.ZERO;
            if (sellerAmount == null) sellerAmount = BigDecimal.ZERO;

            return buyerAmount.add(sellerAmount).doubleValue();
        } catch (Exception e) {
            logger.warn("计算用户交易金额失败: {}", e.getMessage());
            return 0.0;
        }
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int) (Math.random() * 10000));
        return "ORD" + timestamp + String.format("%04d", Integer.parseInt(random));
    }

    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setBuyerId(order.getBuyerId());
        response.setSellerId(order.getSellerId());
        response.setProductId(order.getProductId());
        response.setAmount(order.getAmount());
        response.setStatus(order.getStatus());
        response.setStatusText(getStatusText(order.getStatus()));
        response.setBuyerConfirm(order.getBuyerConfirm());
        response.setSellerConfirm(order.getSellerConfirm());
        response.setTradeType(order.getTradeType());
        response.setTradeTypeText(getTradeTypeText(order.getTradeType()));
        response.setReceiverName(order.getReceiverName());
        response.setReceiverPhone(order.getReceiverPhone());
        response.setCampus(order.getCampus());
        response.setBuilding(order.getBuilding());
        response.setAddressDetail(order.getAddressDetail());
        response.setRemark(order.getRemark());
        response.setCancelReason(order.getCancelReason());
        response.setCancelTime(order.getCancelTime());
        response.setCompleteTime(order.getCompleteTime());
        response.setCreateTime(order.getCreateTime());
        response.setUpdateTime(order.getUpdateTime());

        if (order.getBuyer() != null) {
            response.setBuyerUsername(order.getBuyer().getUsername());
            response.setBuyerNickname(order.getBuyer().getNickname() != null ? order.getBuyer().getNickname() : order.getBuyer().getUsername());
            response.setBuyerPhone(order.getBuyer().getPhone());
            response.setBuyerEmail(order.getBuyer().getEmail());
        } else {
            User buyer = userMapper.selectById(order.getBuyerId());
            if (buyer != null) {
                response.setBuyerUsername(buyer.getUsername());
                response.setBuyerNickname(buyer.getNickname() != null ? buyer.getNickname() : buyer.getUsername());
                response.setBuyerPhone(buyer.getPhone());
                response.setBuyerEmail(buyer.getEmail());
            }
        }

        if (order.getSeller() != null) {
            response.setSellerUsername(order.getSeller().getUsername());
            response.setSellerNickname(order.getSeller().getNickname() != null ? order.getSeller().getNickname() : order.getSeller().getUsername());
            response.setSellerPhone(order.getSeller().getPhone());
            response.setSellerEmail(order.getSeller().getEmail());
        } else {
            User seller = userMapper.selectById(order.getSellerId());
            if (seller != null) {
                response.setSellerUsername(seller.getUsername());
                response.setSellerNickname(seller.getNickname() != null ? seller.getNickname() : seller.getUsername());
                response.setSellerPhone(seller.getPhone());
                response.setSellerEmail(seller.getEmail());
            }
        }

        if (order.getProduct() != null) {
            response.setProductTitle(order.getProduct().getTitle());
            response.setProductImage(extractFirstImage(order.getProduct().getImages()));
            response.setProductPrice(order.getProduct().getPrice());
            response.setProductDescription(order.getProduct().getDescription());
            if (order.getProduct().getCategory() != null) {
                response.setProductCategory(order.getProduct().getCategory().getName());
            }
        } else {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                response.setProductTitle(product.getTitle());
                response.setProductImage(extractFirstImage(product.getImages()));
                response.setProductPrice(product.getPrice());
                response.setProductDescription(product.getDescription());
            }
        }

        return response;
    }

    private void validateReceiverInfo(OrderRequest request) {
        int tradeType = request.getTradeType() == null ? 1 : request.getTradeType();
        if (tradeType != 1 && tradeType != 2) {
            throw new BusinessException("交易方式无效");
        }
        String name = trimToNull(request.getReceiverName());
        String phone = trimToNull(request.getReceiverPhone());
        if (name == null) {
            throw new BusinessException("请填写收货人/联系人");
        }
        if (phone == null) {
            throw new BusinessException("请填写联系电话");
        }
        if (!phone.matches("^1\\d{10}$") && !phone.matches("^0\\d{2,3}-?\\d{7,8}$")) {
            throw new BusinessException("联系电话格式不正确");
        }
        if (tradeType == 1) {
            if (trimToNull(request.getCampus()) == null) {
                throw new BusinessException("面交请填写校区");
            }
            if (trimToNull(request.getBuilding()) == null && trimToNull(request.getAddressDetail()) == null) {
                throw new BusinessException("面交请填写楼栋或详细地点");
            }
        } else {
            if (trimToNull(request.getAddressDetail()) == null) {
                throw new BusinessException("快递配送请填写详细收货地址");
            }
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String getTradeTypeText(Integer tradeType) {
        return Integer.valueOf(2).equals(tradeType) ? "快递配送" : "校内面交";
    }

    private String getStatusText(Integer status) {
        switch (status == null ? 0 : status) {
            case 1: return "待接单";
            case 2: return "进行中";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知状态";
        }
    }

    private String extractFirstImage(String imagesJson) {
        if (imagesJson == null || imagesJson.trim().isEmpty()) return null;

        try {
            if (imagesJson.trim().startsWith("[")) {
                String content = imagesJson.trim().substring(1, imagesJson.trim().length() - 1).trim();
                if (content.isEmpty()) return null;
                String firstImage = content.split(",")[0].trim();
                firstImage = firstImage.replaceAll("^\"|\"$", "");
                return firstImage.isEmpty() ? null : firstImage;
            } else {
                return imagesJson;
            }
        } catch (Exception e) {
            return imagesJson;
        }
    }

    private void clearUserStatsCache(Long userId) {
        try {
            cacheService.delete("user_trade_stats:" + userId);
        } catch (Exception e) {
            logger.warn("清除用户统计缓存失败: {}", e.getMessage());
        }
    }

    private BigDecimal sumAmountByBuyer(Long userId) {
        BigDecimal total = BigDecimal.ZERO;
        var orders = orderMapper.selectList(new LambdaQueryWrapper<Order>().eq(Order::getBuyerId, userId));
        for (var o : orders) {
            if (o.getAmount() != null) total = total.add(o.getAmount());
        }
        return total;
    }

    private BigDecimal sumAmountBySeller(Long userId) {
        BigDecimal total = BigDecimal.ZERO;
        var orders = orderMapper.selectList(new LambdaQueryWrapper<Order>().eq(Order::getSellerId, userId));
        for (var o : orders) {
            if (o.getAmount() != null) total = total.add(o.getAmount());
        }
        return total;
    }

    private BigDecimal sumCompletedAmountByBuyer(Long userId) {
        BigDecimal total = BigDecimal.ZERO;
        var orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getBuyerId, userId).eq(Order::getStatus, 3));
        for (var o : orders) {
            if (o.getAmount() != null) total = total.add(o.getAmount());
        }
        return total;
    }

    private BigDecimal sumCompletedAmountBySeller(Long userId) {
        BigDecimal total = BigDecimal.ZERO;
        var orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getSellerId, userId).eq(Order::getStatus, 3));
        for (var o : orders) {
            if (o.getAmount() != null) total = total.add(o.getAmount());
        }
        return total;
    }
}
