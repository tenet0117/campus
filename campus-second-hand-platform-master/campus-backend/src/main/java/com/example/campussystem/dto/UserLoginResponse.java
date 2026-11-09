package com.example.campussystem.dto;

import java.time.LocalDateTime;

/**
 * 用户登录响应DTO
 */
public class UserLoginResponse {

    private Long userId;
    private String username;
    private String email;
    private String nickname;
    private String phone;
    private String avatar;
    private String role;
    private String token;
    private LocalDateTime loginTime;

    public UserLoginResponse() {}

    public UserLoginResponse(Long userId, String username, String email, String nickname,
                           String phone, String avatar, String role, String token, LocalDateTime loginTime) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.phone = phone;
        this.avatar = avatar;
        this.role = role;
        this.token = token;
        this.loginTime = loginTime;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
}
