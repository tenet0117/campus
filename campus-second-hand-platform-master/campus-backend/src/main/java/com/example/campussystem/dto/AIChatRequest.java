package com.example.campussystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI聊天请求DTO
 */
@Data
public class AIChatRequest {
    
    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;
    
    /**
     * 会话ID（可选，用于保持上下文）
     */
    private String sessionId;
    
    /**
     * 商品ID（可选，用于商品相关咨询）
     */
    private Long productId;
    
    /**
     * 订单ID（可选，用于订单相关咨询）
     */
    private Long orderId;
    
    // 手动添加 getter 方法以确保兼容性
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
