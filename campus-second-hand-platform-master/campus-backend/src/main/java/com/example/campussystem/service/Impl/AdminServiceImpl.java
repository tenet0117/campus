package com.example.campussystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campussystem.dto.OrderResponse;
import com.example.campussystem.dto.ProductResponse;
import com.example.campussystem.dto.UserProfileResponse;
import com.example.campussystem.entity.*;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.mapper.*;
import com.example.campussystem.service.AdminService;
import com.example.campussystem.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private AdminMapper adminMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        long totalUsers = userMapper.selectCount(null);
        long activeUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        long newUsersToday = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, LocalDateTime.now().minusDays(1)));
        long newUsersWeek = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, LocalDateTime.now().minusDays(7)));
        long newUsersPrevWeek = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, LocalDateTime.now().minusDays(14))
                .lt(User::getCreateTime, LocalDateTime.now().minusDays(7)));

        long totalProducts = productMapper.selectCount(null);
        long activeProducts = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1));
        long reservedProducts = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 2));
        long soldProducts = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 3));

        long totalOrders = orderMapper.selectCount(null);
        long pendingOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1));
        long processingOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 2));
        long completedOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3));
        long cancelledOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 4));

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal amountWeek = BigDecimal.ZERO;
        BigDecimal amountPrevWeek = BigDecimal.ZERO;
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusDays(14);
        var completedOrderList = orderMapper.selectList(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3));
        for (var o : completedOrderList) {
            if (o.getAmount() == null) continue;
            totalAmount = totalAmount.add(o.getAmount());
            if (o.getCompleteTime() != null) {
                if (!o.getCompleteTime().isBefore(weekAgo)) {
                    amountWeek = amountWeek.add(o.getAmount());
                } else if (!o.getCompleteTime().isBefore(twoWeeksAgo)) {
                    amountPrevWeek = amountPrevWeek.add(o.getAmount());
                }
            } else if (o.getCreateTime() != null && !o.getCreateTime().isBefore(weekAgo)) {
                amountWeek = amountWeek.add(o.getAmount());
            }
        }

        long ordersWeek = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, weekAgo));
        long ordersPrevWeek = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, twoWeeksAgo)
                .lt(Order::getCreateTime, weekAgo));

        long totalCategories = categoryMapper.selectCount(null);

        statistics.put("users", Map.of(
                "total", totalUsers,
                "active", activeUsers,
                "newToday", newUsersToday,
                "newWeek", newUsersWeek,
                "newPrevWeek", newUsersPrevWeek
        ));
        statistics.put("products", Map.of(
                "total", totalProducts,
                "active", activeProducts,
                "reserved", reservedProducts,
                "sold", soldProducts
        ));
        statistics.put("orders", Map.of(
                "total", totalOrders,
                "pending", pendingOrders,
                "processing", processingOrders,
                "completed", completedOrders,
                "cancelled", cancelledOrders,
                "totalAmount", totalAmount,
                "amountWeek", amountWeek,
                "amountPrevWeek", amountPrevWeek,
                "countWeek", ordersWeek,
                "countPrevWeek", ordersPrevWeek
        ));
        statistics.put("categories", Map.of("total", totalCategories));

        return statistics;
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> getUserList(int pageNum, int size, String sortBy, String sortDir, String keyword) {
        Page<User> mpPage = new Page<>(pageNum, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getNickname, keyword));
        }

        wrapper.orderBy(true, "ASC".equalsIgnoreCase(sortDir),
                "username".equals(sortBy) ? User::getUsername :
                "email".equals(sortBy) ? User::getEmail :
                "createTime".equals(sortBy) ? User::getCreateTime : User::getId);

        Page<User> users = userMapper.selectPage(mpPage, wrapper);

        Page<UserProfileResponse> result = new Page<>(users.getCurrent(), users.getSize(), users.getTotal());
        result.setRecords(users.getRecords().stream().map(user -> {
            UserProfileResponse response = new UserProfileResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setNickname(user.getNickname());
            response.setPhone(user.getPhone());
            response.setEmail(user.getEmail());
            response.setAvatar(user.getAvatar());
            response.setStatus(user.getStatus());
            response.setRole(user.getRole());
            response.setCreateTime(user.getCreateTime());
            response.setUpdateTime(user.getUpdateTime());
            return response;
        }).collect(Collectors.toList()));
        return result;
    }

    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        if (status < 0 || status > 1) throw new BusinessException("用户状态值无效");

        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductList(int pageNum, int size, String sortBy, String sortDir,
                                              String keyword, Integer status) {
        Page<Product> mpPage = new Page<>(pageNum, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Product::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderBy(true, "ASC".equalsIgnoreCase(sortDir),
                "price".equals(sortBy) ? Product::getPrice :
                "viewCount".equals(sortBy) ? Product::getViewCount :
                "createTime".equals(sortBy) ? Product::getCreateTime : Product::getId);

        Page<Product> products = productMapper.selectPage(mpPage, wrapper);
        Page<ProductResponse> result = new Page<>(products.getCurrent(), products.getSize(), products.getTotal());
        result.setRecords(products.getRecords().stream().map(ProductResponse::new).collect(Collectors.toList()));
        return result;
    }

    public void updateProductStatus(Long productId, Integer status) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new BusinessException("商品不存在");

        if (status < 0 || status > 3) throw new BusinessException("商品状态值无效");

        product.setStatus(status);
        productMapper.updateById(product);
    }

    public void forceDeleteProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new BusinessException("商品不存在");

        long pendingOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getProductId, productId)
                .in(Order::getStatus, List.of(1, 2)));
        if (pendingOrders > 0) {
            throw new BusinessException("该商品存在未完成的订单，无法删除");
        }

        productMapper.deleteById(productId);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrderList(int pageNum, int size, String sortBy, String sortDir,
                                           String keyword, Integer status) {
        Page<Order> mpPage = new Page<>(pageNum, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Order::getOrderNo, keyword);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderBy(true, "ASC".equalsIgnoreCase(sortDir),
                "createTime".equals(sortBy) ? Order::getCreateTime :
                "amount".equals(sortBy) ? Order::getAmount : Order::getId);

        Page<Order> orders = orderMapper.selectPage(mpPage, wrapper);
        Page<OrderResponse> result = new Page<>(orders.getCurrent(), orders.getSize(), orders.getTotal());
        result.setRecords(orders.getRecords().stream().map(this::convertOrderToResponse).collect(Collectors.toList()));
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");

        Map<String, Object> detail = new HashMap<>();
        detail.put("order", convertOrderToResponse(order));
        detail.put("statistics", Map.of(
                "orderNo", order.getOrderNo(),
                "createTime", order.getCreateTime(),
                "updateTime", order.getUpdateTime()
        ));
        return detail;
    }

    public void updateOrderStatus(Long orderId, Integer status) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");

        if (status < 1 || status > 4) throw new BusinessException("订单状态值无效");

        order.setStatus(status);
        if (status == 3) {
            order.setCompleteTime(LocalDateTime.now());
            order.setBuyerConfirm(1);
            order.setSellerConfirm(1);
        } else if (status == 4) {
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason("管理员操作");
        }

        orderMapper.updateById(order);
    }

    public void deleteOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        orderMapper.deleteById(orderId);
    }

    private OrderResponse convertOrderToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setBuyerId(order.getBuyerId());
        response.setSellerId(order.getSellerId());
        response.setProductId(order.getProductId());
        response.setAmount(order.getAmount());
        response.setStatus(order.getStatus());
        response.setStatusText(getOrderStatusText(order.getStatus()));
        response.setBuyerConfirm(order.getBuyerConfirm());
        response.setSellerConfirm(order.getSellerConfirm());
        response.setRemark(order.getRemark());
        response.setCancelReason(order.getCancelReason());
        response.setCancelTime(order.getCancelTime());
        response.setCompleteTime(order.getCompleteTime());
        response.setCreateTime(order.getCreateTime());
        response.setUpdateTime(order.getUpdateTime());
        response.setTradeType(order.getTradeType());
        response.setTradeTypeText(order.getTradeType() != null && order.getTradeType() == 2 ? "快递" : "面交");
        response.setReceiverName(order.getReceiverName());
        response.setReceiverPhone(order.getReceiverPhone());
        response.setCampus(order.getCampus());
        response.setBuilding(order.getBuilding());
        response.setAddressDetail(order.getAddressDetail());

        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            response.setBuyerUsername(buyer.getUsername());
            response.setBuyerNickname(buyer.getNickname());
            response.setBuyerPhone(buyer.getPhone());
            response.setBuyerEmail(buyer.getEmail());
        }

        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            response.setSellerUsername(seller.getUsername());
            response.setSellerNickname(seller.getNickname());
            response.setSellerPhone(seller.getPhone());
            response.setSellerEmail(seller.getEmail());
        }

        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            response.setProductTitle(product.getTitle());
            response.setProductImage(extractFirstImage(product.getImages()));
            response.setProductPrice(product.getPrice());
            response.setProductDescription(product.getDescription());
            if (product.getCategory() != null) {
                response.setProductCategory(product.getCategory().getName());
            }
        }

        return response;
    }

    private String getOrderStatusText(Integer status) {
        switch (status == null ? 0 : status) {
            case 1: return "待确认";
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

    @Transactional(readOnly = true)
    public Map<String, Object> getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        UserProfileResponse userResponse = new UserProfileResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setNickname(user.getNickname());
        userResponse.setPhone(user.getPhone());
        userResponse.setEmail(user.getEmail());
        userResponse.setAvatar(user.getAvatar());
        userResponse.setStatus(user.getStatus());
        userResponse.setRole(user.getRole());
        userResponse.setCreateTime(user.getCreateTime());
        userResponse.setUpdateTime(user.getUpdateTime());

        long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, userId));
        long buyOrderCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getBuyerId, userId));
        long sellOrderCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getSellerId, userId));
        userResponse.setProductCount((int) productCount);
        userResponse.setBuyOrderCount((int) buyOrderCount);
        userResponse.setSellOrderCount((int) sellOrderCount);

        Map<String, Object> detail = new HashMap<>();
        detail.put("userInfo", userResponse);
        detail.put("stats", Map.of(
                "productCount", productCount,
                "buyOrderCount", buyOrderCount,
                "sellOrderCount", sellOrderCount
        ));
        return detail;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProductDetail(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new BusinessException("商品不存在");

        if (product.getUserId() != null) {
            product.setUser(userMapper.selectById(product.getUserId()));
        }
        if (product.getCategoryId() != null) {
            product.setCategory(categoryMapper.selectById(product.getCategoryId()));
        }

        ProductResponse productResponse = new ProductResponse(product);
        Map<String, Object> detail = new HashMap<>();
        detail.put("product", productResponse);
        return detail;
    }

    public void batchUpdateUserStatus(List<Long> userIds, Integer status) {
        for (Long userId : userIds) {
            updateUserStatus(userId, status);
        }
    }

    public void batchUpdateProductStatus(List<Long> productIds, Integer status) {
        for (Long productId : productIds) {
            updateProductStatus(productId, status);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserActivityStatistics(int days) {
        Map<String, Object> result = new HashMap<>();
        result.put("days", days);
        long totalUsers = userMapper.selectCount(null);
        long activeUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        result.put("totalUsers", totalUsers);
        result.put("activeUsers", activeUsers);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCategoryStatistics() {
        Map<String, Object> result = new HashMap<>();
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Category c : categories) {
            long count = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, c.getId()));
            Map<String, Object> item = new HashMap<>();
            item.put("name", c.getName());
            item.put("count", count);
            list.add(item);
        }
        result.put("categories", list);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTradeStatistics(int days) {
        Map<String, Object> result = new HashMap<>();
        int safeDays = Math.max(1, Math.min(days, 90));
        LocalDateTime since = LocalDateTime.now().minusDays(safeDays).toLocalDate().atStartOfDay();

        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, since)
                .orderByAsc(Order::getCreateTime));

        Map<String, Long> countByDay = new HashMap<>();
        Map<String, BigDecimal> amountByDay = new HashMap<>();
        Map<String, Long> completedByDay = new HashMap<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MM-dd");

        for (int i = safeDays - 1; i >= 0; i--) {
            String key = LocalDateTime.now().minusDays(i).format(fmt);
            countByDay.put(key, 0L);
            amountByDay.put(key, BigDecimal.ZERO);
            completedByDay.put(key, 0L);
        }

        for (Order o : orders) {
            if (o.getCreateTime() == null) continue;
            String key = o.getCreateTime().format(fmt);
            if (!countByDay.containsKey(key)) continue;
            countByDay.put(key, countByDay.get(key) + 1);
            if (Integer.valueOf(3).equals(o.getStatus()) && o.getAmount() != null) {
                amountByDay.put(key, amountByDay.get(key).add(o.getAmount()));
                completedByDay.put(key, completedByDay.get(key) + 1);
            }
        }

        List<String> labels = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        List<Long> completedCounts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        for (int i = safeDays - 1; i >= 0; i--) {
            String key = LocalDateTime.now().minusDays(i).format(fmt);
            labels.add(key);
            orderCounts.add(countByDay.get(key));
            completedCounts.add(completedByDay.get(key));
            amounts.add(amountByDay.get(key));
        }

        result.put("days", safeDays);
        result.put("orderCount", orders.size());
        result.put("labels", labels);
        result.put("orderCounts", orderCounts);
        result.put("completedCounts", completedCounts);
        result.put("amounts", amounts);
        return result;
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getSystemLogs(int pageNum, int size, String level, String keyword) {
        Page<Map<String, Object>> mpPage = new Page<>(pageNum, size);
        mpPage.setRecords(new ArrayList<>());
        mpPage.setTotal(0);
        return mpPage;
    }

    public void cleanupSystemData(int days) {
    }

    public void deleteAdmin(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) throw new BusinessException("管理员不存在");
        adminMapper.deleteById(adminId);
    }
}
