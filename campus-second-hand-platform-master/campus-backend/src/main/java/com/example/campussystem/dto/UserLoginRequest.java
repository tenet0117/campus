package com.example.campussystem.dto;

import jakarta.validation.constraints.*;

/**
 * 用户登录请求DTO - 支持用户名或邮箱登录
 */
public class UserLoginRequest {

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应为6-20位")
    private String password;

    public UserLoginRequest() {}

    public UserLoginRequest(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserLoginRequest{" +
                "account='" + account + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
