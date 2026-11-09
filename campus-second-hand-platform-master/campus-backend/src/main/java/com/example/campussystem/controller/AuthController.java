package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.dto.UserLoginRequest;
import com.example.campussystem.dto.UserLoginResponse;
import com.example.campussystem.dto.UserProfileResponse;
import com.example.campussystem.dto.UserRegisterRequest;
import com.example.campussystem.dto.UserRegisterResponse;
import com.example.campussystem.dto.UserUpdateRequest;
import com.example.campussystem.service.UserService;
import com.example.campussystem.util.JwtUtil;
import com.example.campussystem.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 提供用户认证相关的API接口
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegisterResponse>> register(@Valid @RequestBody UserRegisterRequest request) {
        UserRegisterResponse response = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "注册成功"));
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Void>> health() {
        return ResponseEntity.ok(ApiResponse.success("AuthController is working"));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(@Valid @RequestBody UserLoginRequest request) {
        UserLoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "登录成功"));
    }



    /**
     * 获取当前用户详细信息（需要认证）
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("用户未登录"));
        }
        
        UserProfileResponse profile = userService.getUserProfile(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(profile, "获取用户信息成功"));
    }

    /**
     * 更新当前用户信息（需要认证）
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("用户未登录"));
        }
        
        UserProfileResponse updatedProfile = userService.updateUserProfile(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success(updatedProfile, "用户信息更新成功"));
    }



    /**
     * 获取用户统计信息（需要认证）
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("用户未登录"));
        }
        
        Map<String, Object> stats = userService.getUserTradeStats(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(stats, "获取用户统计成功"));
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("无效的token"));
        }
        
        String newToken = jwtUtil.refreshToken(token);
        if (newToken == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("token刷新失败"));
        }
        
        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);
        
        return ResponseEntity.ok(ApiResponse.success(result, "token刷新成功"));
    }
}