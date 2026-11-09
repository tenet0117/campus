package com.example.campussystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campussystem.dto.AdminLoginRequest;
import com.example.campussystem.dto.AdminResponse;
import com.example.campussystem.dto.DashboardStatsResponse;
import com.example.campussystem.entity.*;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.mapper.*;
import com.example.campussystem.service.AdminAuthService;
import com.example.campussystem.util.JwtUtil;
import com.example.campussystem.util.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminAuthServiceImpl implements AdminAuthService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> login(AdminLoginRequest request, HttpServletRequest httpRequest) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, request.getUsername())
                .eq(Admin::getStatus, 1));
        if (admin == null) throw new BusinessException("用户名或密码错误");

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(getClientIpAddress(httpRequest));
        admin.setLoginCount(admin.getLoginCount() + 1);
        adminMapper.updateById(admin);

        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("admin", new AdminResponse(admin));
        result.put("permissions", getAllPermissions());

        return result;
    }

    @Transactional(readOnly = true)
    public AdminResponse getAdminInfo(String username) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (admin == null) throw new BusinessException("管理员不存在");
        return new AdminResponse(admin);
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        stats.setTotalUsers(userMapper.selectCount(null));
        stats.setTotalProducts(productMapper.selectCount(null));
        stats.setTotalOrders(orderMapper.selectCount(null));
        stats.setTotalMessages(0L);

        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        stats.setTodayNewUsers(userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)));
        stats.setTodayNewProducts(productMapper.selectCount(new LambdaQueryWrapper<Product>().ge(Product::getCreateTime, todayStart)));
        stats.setTodayNewOrders(orderMapper.selectCount(new LambdaQueryWrapper<Order>().ge(Order::getCreateTime, todayStart)));

        BigDecimal todayAmount = BigDecimal.ZERO;
        var todayOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart).eq(Order::getStatus, 3));
        for (var o : todayOrders) {
            if (o.getAmount() != null) todayAmount = todayAmount.add(o.getAmount());
        }
        stats.setTodayTransactionAmount(todayAmount);

        stats.setActiveUsers(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, 1)));
        stats.setOnlineUsers(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getOnline, true)));
        stats.setAvailableProducts(productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)));
        stats.setSoldProducts(productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 3)));
        stats.setPendingOrders(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1)));
        stats.setCompletedOrders(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3)));

        stats.setCategoryStats(getCategoryStatsList());
        stats.setUserActivityStats(getUserActivityStatsList());
        stats.setTransactionStats(getTransactionStatsList());
        stats.setPopularProducts(getPopularProductsList());
        stats.setRecentUsers(getRecentUsersList());

        return stats;
    }

    public AdminResponse createAdmin(Admin admin) {
        if (adminMapper.exists(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, admin.getUsername()))) {
            throw new BusinessException("管理员用户名已存在");
        }

        if (admin.getEmail() != null && adminMapper.exists(new LambdaQueryWrapper<Admin>().eq(Admin::getEmail, admin.getEmail()))) {
            throw new BusinessException("邮箱已被使用");
        }

        admin.setPassword(passwordEncoder.encode(admin.getPassword()));

        if (admin.getRole() == null) admin.setRole("ADMIN");
        if (admin.getStatus() == null) admin.setStatus(1);
        if (admin.getLoginCount() == null) admin.setLoginCount(0);

        adminMapper.insert(admin);
        return new AdminResponse(admin);
    }

    @Transactional(readOnly = true)
    public Page<AdminResponse> getAdminList(int pageNum, int size, String keyword, Integer status) {
        Page<Admin> mpPage = new Page<>(pageNum, size);
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<Admin>()
                .orderByDesc(Admin::getCreateTime);

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Admin::getUsername, keyword);
        }
        if (status != null) {
            wrapper.eq(Admin::getStatus, status);
        }

        Page<Admin> admins = adminMapper.selectPage(mpPage, wrapper);
        Page<AdminResponse> result = new Page<>(admins.getCurrent(), admins.getSize(), admins.getTotal());
        result.setRecords(admins.getRecords().stream().map(AdminResponse::new).collect(Collectors.toList()));
        return result;
    }

    public void updateAdminStatus(Long adminId, Integer status) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) throw new BusinessException("管理员不存在");

        admin.setStatus(status);
        adminMapper.updateById(admin);
    }

    public void deleteAdmin(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) throw new BusinessException("管理员不存在");

        long adminCount = adminMapper.selectCount(new LambdaQueryWrapper<Admin>().eq(Admin::getStatus, 1));
        if (adminCount <= 1) {
            throw new BusinessException("不能删除最后一个管理员");
        }

        adminMapper.deleteById(adminId);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        try {
            return IpUtils.getClientIpAddress(request);
        } catch (Exception e) {
            return "unknown";
        }
    }

    private List<String> getAllPermissions() {
        return Arrays.asList(
                "user:view", "user:edit", "user:delete",
                "product:view", "product:edit", "product:delete",
                "order:view", "order:edit", "order:delete",
                "category:view", "category:edit", "category:delete",
                "admin:view", "admin:edit", "admin:delete",
                "system:view", "system:manage", "system:logs",
                "statistics:view", "cache:manage"
        );
    }

    private List<DashboardStatsResponse.CategoryStats> getCategoryStatsList() {
        List<Category> categories = categoryMapper.selectList(null);
        return categories.stream().map(category -> {
            Long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                    .eq(Product::getCategoryId, category.getId()));
            Long orderCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                    .eq(Order::getProductId, category.getId())); // approximate
            BigDecimal totalAmount = BigDecimal.ZERO;
            var orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .eq(Order::getStatus, 3));
            for (var o : orders) {
                Product product = productMapper.selectById(o.getProductId());
                if (product != null && product.getCategoryId().equals(category.getId()) && o.getAmount() != null) {
                    totalAmount = totalAmount.add(o.getAmount());
                }
            }

            return new DashboardStatsResponse.CategoryStats(
                    category.getName(), productCount, orderCount, totalAmount);
        }).collect(Collectors.toList());
    }

    private List<DashboardStatsResponse.DailyStats> getUserActivityStatsList() {
        List<DashboardStatsResponse.DailyStats> stats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59);

            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .ge(User::getCreateTime, startOfDay).le(User::getCreateTime, endOfDay));
            stats.add(new DashboardStatsResponse.DailyStats(date.format(formatter), count, BigDecimal.ZERO));
        }

        return stats;
    }

    private List<DashboardStatsResponse.DailyStats> getTransactionStatsList() {
        List<DashboardStatsResponse.DailyStats> stats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59);

            Long count = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreateTime, startOfDay).le(Order::getCreateTime, endOfDay)
                    .eq(Order::getStatus, 3));

            BigDecimal amount = BigDecimal.ZERO;
            var orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreateTime, startOfDay).le(Order::getCreateTime, endOfDay)
                    .eq(Order::getStatus, 3));
            for (var o : orders) {
                if (o.getAmount() != null) amount = amount.add(o.getAmount());
            }

            stats.add(new DashboardStatsResponse.DailyStats(date.format(formatter), count, amount));
        }

        return stats;
    }

    private List<DashboardStatsResponse.PopularProduct> getPopularProductsList() {
        Page<Product> mpPage = new Page<>(0, 10);
        Page<Product> products = productMapper.selectPage(mpPage, new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getViewCount));

        return products.getRecords().stream().map(product -> {
            DashboardStatsResponse.PopularProduct popularProduct = new DashboardStatsResponse.PopularProduct();
            popularProduct.setId(product.getId());
            popularProduct.setTitle(product.getTitle());
            popularProduct.setPrice(product.getPrice());
            popularProduct.setViewCount(product.getViewCount());
            if (product.getCategory() != null) {
                popularProduct.setCategoryName(product.getCategory().getName());
            }
            return popularProduct;
        }).collect(Collectors.toList());
    }

    private List<DashboardStatsResponse.RecentUser> getRecentUsersList() {
        Page<User> mpPage = new Page<>(0, 10);
        Page<User> users = userMapper.selectPage(mpPage, new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime));

        return users.getRecords().stream().map(user -> {
            DashboardStatsResponse.RecentUser recentUser = new DashboardStatsResponse.RecentUser();
            recentUser.setId(user.getId());
            recentUser.setUsername(user.getUsername());
            recentUser.setEmail(user.getEmail());
            recentUser.setNickname(user.getNickname());
            recentUser.setCreateTime(user.getCreateTime());
            return recentUser;
        }).collect(Collectors.toList());
    }
}
