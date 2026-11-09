package com.example.campussystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campussystem.dto.AdminLoginRequest;
import com.example.campussystem.dto.AdminResponse;
import com.example.campussystem.dto.DashboardStatsResponse;
import com.example.campussystem.entity.Admin;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AdminAuthService {

    Map<String, Object> login(AdminLoginRequest request, HttpServletRequest httpRequest);

    AdminResponse getAdminInfo(String username);

    DashboardStatsResponse getDashboardStats();

    AdminResponse createAdmin(Admin admin);

    Page<AdminResponse> getAdminList(int pageNum, int size, String keyword, Integer status);

    void updateAdminStatus(Long adminId, Integer status);

    void deleteAdmin(Long adminId);
}
