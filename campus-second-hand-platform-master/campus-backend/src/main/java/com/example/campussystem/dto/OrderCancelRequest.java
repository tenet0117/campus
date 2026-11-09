package com.example.campussystem.dto;

import jakarta.validation.constraints.Size;

/**
 * 订单取消请求DTO
 */
public class OrderCancelRequest {

    @Size(max = 500, message = "取消原因长度不能超过500个字符")
    private String reason;

    // 构造函数
    public OrderCancelRequest() {}

    public OrderCancelRequest(String reason) {
        this.reason = reason;
    }

    // Getter和Setter方法
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "OrderCancelRequest{" +
                "reason='" + reason + '\'' +
                '}';
    }
}

