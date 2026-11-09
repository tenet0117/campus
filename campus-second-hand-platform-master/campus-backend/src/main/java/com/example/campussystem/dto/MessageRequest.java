package com.example.campussystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 消息请求DTO
 */
public class MessageRequest {

    @NotNull(message = "接收者ID不能为空")
    private Long toUserId;

    private Long productId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容长度不能超过1000个字符")
    private String content;

    private Integer type = 1; // 1文本,2图片

    // 构造函数
    public MessageRequest() {}

    public MessageRequest(Long toUserId, String content) {
        this.toUserId = toUserId;
        this.content = content;
    }

    public MessageRequest(Long toUserId, Long productId, String content) {
        this.toUserId = toUserId;
        this.productId = productId;
        this.content = content;
    }

    public MessageRequest(Long toUserId, Long productId, String content, Integer type) {
        this.toUserId = toUserId;
        this.productId = productId;
        this.content = content;
        this.type = type;
    }

    // Getter和Setter方法
    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MessageRequest{" +
                "toUserId=" + toUserId +
                ", productId=" + productId +
                ", content='" + content + '\'' +
                ", type=" + type +
                '}';
    }
}