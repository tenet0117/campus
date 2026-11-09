package com.example.campussystem.dto;

import java.time.LocalDateTime;

/**
 * 对话响应DTO
 */
public class ConversationResponse {

    private Long contactUserId;
    private String contactUsername;
    private String contactNickname;
    private String contactAvatar;
    private String lastMessageContent;
    private Integer lastMessageType;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
    private Long productId;
    private String productTitle;
    private String productImage;

    // 构造函数
    public ConversationResponse() {}

    public ConversationResponse(Long contactUserId, String contactUsername, String contactNickname, String contactAvatar,
                               String lastMessageContent, Integer lastMessageType, LocalDateTime lastMessageTime,
                               Long unreadCount, Long productId, String productTitle, String productImage) {
        this.contactUserId = contactUserId;
        this.contactUsername = contactUsername;
        this.contactNickname = contactNickname;
        this.contactAvatar = contactAvatar;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageType = lastMessageType;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
        this.productId = productId;
        this.productTitle = productTitle;
        this.productImage = productImage;
    }

    // Getter和Setter方法
    public Long getContactUserId() {
        return contactUserId;
    }

    public void setContactUserId(Long contactUserId) {
        this.contactUserId = contactUserId;
    }

    public String getContactUsername() {
        return contactUsername;
    }

    public void setContactUsername(String contactUsername) {
        this.contactUsername = contactUsername;
    }

    public String getContactNickname() {
        return contactNickname;
    }

    public void setContactNickname(String contactNickname) {
        this.contactNickname = contactNickname;
    }

    public String getContactAvatar() {
        return contactAvatar;
    }

    public void setContactAvatar(String contactAvatar) {
        this.contactAvatar = contactAvatar;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public Integer getLastMessageType() {
        return lastMessageType;
    }

    public void setLastMessageType(Integer lastMessageType) {
        this.lastMessageType = lastMessageType;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    @Override
    public String toString() {
        return "ConversationResponse{" +
                "contactUserId=" + contactUserId +
                ", contactUsername='" + contactUsername + '\'' +
                ", contactNickname='" + contactNickname + '\'' +
                ", contactAvatar='" + contactAvatar + '\'' +
                ", lastMessageContent='" + lastMessageContent + '\'' +
                ", lastMessageType=" + lastMessageType +
                ", lastMessageTime=" + lastMessageTime +
                ", unreadCount=" + unreadCount +
                ", productId=" + productId +
                ", productTitle='" + productTitle + '\'' +
                ", productImage='" + productImage + '\'' +
                '}';
    }
}