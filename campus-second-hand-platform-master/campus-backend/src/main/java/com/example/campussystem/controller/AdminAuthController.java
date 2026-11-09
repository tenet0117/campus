package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.dto.AdminLoginRequest;
import com.example.campussystem.dto.AdminResponse;
import com.example.campussystem.dto.DashboardStatsResponse;
import com.example.campussystem.entity.Admin;
import com.example.campussystem.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;

/**
 * 管理员认证控制器
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "管理员认证", description = "管理员登录、权限管理等接口")
@Validated
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员用户名密码登录")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody AdminLoginRequest request,
                                                 HttpServletRequest httpRequest) {
        Map<String, Object> result = adminAuthService.login(request, httpRequest);
        return ApiResponse.success(result, "登录成功");
    }

    @GetMapping("/info")
    @Operation(summary = "获取管理员信息", description = "获取当前登录管理员的详细信息")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminResponse> getAdminInfo(Principal principal) {
        AdminResponse admin = adminAuthService.getAdminInfo(principal.getName());
        return ApiResponse.success(admin);
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "获取仪表盘统计数据", description = "获取管理后台首页的统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DashboardStatsResponse> getDashboardStats() {
        DashboardStatsResponse stats = adminAuthService.getDashboardStats();
        return ApiResponse.success(stats);
    }

    @PostMapping("/create")
    @Operation(summary = "创建管理员", description = "创建新的管理员账户")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminResponse> createAdmin(@Valid @RequestBody Admin admin) {
        AdminResponse result = adminAuthService.createAdmin(admin);
        return ApiResponse.success(result, "管理员创建成功");
    }

    @GetMapping("/list")
    @Operation(summary = "获取管理员列表", description = "分页获取管理员列表")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<AdminResponse>> getAdminList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<AdminResponse> result = adminAuthService.getAdminList(page, size, keyword, status);
        return ApiResponse.success(result);
    }

    @PutMapping("/{adminId}/status")
    @Operation(summary = "更新管理员状态", description = "启用或禁用管理员账户")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateAdminStatus(@PathVariable Long adminId,
                                              @RequestParam Integer status) {
        adminAuthService.updateAdminStatus(adminId, status);
        return ApiResponse.success(null, "管理员状态更新成功");
    }

    @DeleteMapping("/{adminId}")
    @Operation(summary = "删除管理员", description = "删除指定的管理员账户")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAdmin(@PathVariable Long adminId) {
        adminAuthService.deleteAdmin(adminId);
        return ApiResponse.success(null, "管理员删除成功");
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员登出", description = "管理员退出登录")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> logout() {
        // JWT是无状态的，客户端删除token即可
        return ApiResponse.success(null, "登出成功");
    }
}
