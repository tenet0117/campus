package com.example.campussystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.campussystem.dto.UserLoginRequest;
import com.example.campussystem.dto.UserLoginResponse;
import com.example.campussystem.dto.UserProfileResponse;
import com.example.campussystem.dto.UserRegisterRequest;
import com.example.campussystem.dto.UserRegisterResponse;
import com.example.campussystem.dto.UserUpdateRequest;
import com.example.campussystem.entity.User;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.mapper.OrderMapper;
import com.example.campussystem.mapper.UserMapper;
import com.example.campussystem.service.UserService;
import com.example.campussystem.service.CacheService;
import com.example.campussystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private OrderMapper orderMapper;

    private static final String CACHE_KEY_USER_PROFILE = "user_profile:";

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        String account = request.getAccount();
        
        // 支持用户名或邮箱登录
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, account));
        if (user == null) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, account));
        }
        
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用，请联系管理员");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String role = user.getRole() != null ? user.getRole() : "USER";
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);

        return new UserLoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getPhone(),
                user.getAvatar(),
                role,
                token,
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public UserRegisterResponse register(UserRegisterRequest request) {
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()))) {
            throw new BusinessException("用户名已存在");
        }

        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()))) {
            throw new BusinessException("邮箱已注册");
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()))) {
                throw new BusinessException("手机号已被使用");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setStatus(1);
        user.setAvatar("avatars/default-avatar.png");

        userMapper.insert(user);

        return new UserRegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getCreateTime()
        );
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)));
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    @Override
    public boolean isPhoneExists(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        return userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        String cacheKey = CACHE_KEY_USER_PROFILE + userId;

        UserProfileResponse cachedProfile = cacheService.get(cacheKey, UserProfileResponse.class);
        if (cachedProfile != null) return cachedProfile;

        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        UserProfileResponse response = buildProfileResponse(user);
        cacheService.set(cacheKey, response, 30, TimeUnit.MINUTES);
        return response;
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail())) {
                User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
                if (existing != null && !existing.getId().equals(userId)) {
                    throw new BusinessException("邮箱已被其他用户使用");
                }
            }
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
            if (existing != null && !existing.getId().equals(userId)) {
                throw new BusinessException("手机号已被其他用户使用");
            }
        }

        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());

        userMapper.updateById(user);
        cacheService.delete(CACHE_KEY_USER_PROFILE + userId);

        return buildProfileResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserTradeStats(Long userId) {
        String cacheKey = "user_trade_stats:" + userId;

        Map<String, Object> cachedStats = cacheService.get(cacheKey, Map.class);
        if (cachedStats != null) return cachedStats;

        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        Map<String, Object> stats = new HashMap<>();
        try {
            Long successCount = orderMapper.selectCount(new LambdaQueryWrapper<com.example.campussystem.entity.Order>()
                    .eq(com.example.campussystem.entity.Order::getStatus, 3)
                    .and(w -> w.eq(com.example.campussystem.entity.Order::getBuyerId, userId)
                              .or().eq(com.example.campussystem.entity.Order::getSellerId, userId)));

            BigDecimal totalAmount = BigDecimal.ZERO;
            var orders = orderMapper.selectList(new LambdaQueryWrapper<com.example.campussystem.entity.Order>()
                    .eq(com.example.campussystem.entity.Order::getStatus, 3)
                    .and(w -> w.eq(com.example.campussystem.entity.Order::getBuyerId, userId)
                              .or().eq(com.example.campussystem.entity.Order::getSellerId, userId)));
            for (var o : orders) {
                if (o.getAmount() != null) totalAmount = totalAmount.add(o.getAmount());
            }

            stats.put("successCount", successCount);
            stats.put("totalAmount", totalAmount.doubleValue());
            stats.put("lastUpdated", LocalDateTime.now());
        } catch (Exception e) {
            stats.put("successCount", 0L);
            stats.put("totalAmount", 0.0);
            stats.put("error", "数据查询失败");
        }

        cacheService.set(cacheKey, stats, 15, TimeUnit.MINUTES);
        return stats;
    }

    private UserProfileResponse buildProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        response.setUpdateTime(user.getUpdateTime());
        return response;
    }
}
