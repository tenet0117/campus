package com.example.campussystem.dto;

import java.time.LocalDateTime;

/**
 * 消息响应DTO
 */
public class MessageResponse {

    private Long id;
    private Long fromUserId;
    private String fromUsername;
    private String fromNickname;
    private String fromAvatar;
    private Long toUserId;
    private String toUsername;
    private String toNickname;
    private String toAvatar;
    private Long productId;
    private String productTitle;
    private String productImage;
    private String content;
    private Integer type;
    private String typeText;
    private Integer isRead;
    private LocalDateTime createTime;
    private LocalDateTime readTime; // 已读时间
    
    // 消息状态相关
    private Boolean isUnread; // 是否未读
    private String readStatus; // 已读状态文本：未读/已读/已发送

    // 构造函数
    public MessageResponse() {}

    public MessageResponse(Long id, Long fromUserId, String fromUsername, String fromNickname, String fromAvatar,
                          Long toUserId, String toUsername, String toNickname, String toAvatar,
                          Long productId, String productTitle, String productImage,
                          String content, Integer type, String typeText, Integer isRead, LocalDateTime createTime) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.fromUsername = fromUsername;
        this.fromNickname = fromNickname;
        this.fromAvatar = fromAvatar;
        this.toUserId = toUserId;
        this.toUsername = toUsername;
        this.toNickname = toNickname;
        this.toAvatar = toAvatar;
        this.productId = productId;
        this.productTitle = productTitle;
        this.productImage = productImage;
        this.content = content;
        this.type = type;
        this.typeText = typeText;
        this.isRead = isRead;
        this.createTime = createTime;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getFromNickname() {
        return fromNickname;
    }

    public void setFromNickname(String fromNickname) {
        this.fromNickname = fromNickname;
    }

    public String getFromAvatar() {
        return fromAvatar;
    }

    public void setFromAvatar(String fromAvatar) {
        this.fromAvatar = fromAvatar;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }

    public String getToNickname() {
        return toNickname;
    }

    public void setToNickname(String toNickname) {
        this.toNickname = toNickname;
    }

    public String getToAvatar() {
        return toAvatar;
    }

    public void setToAvatar(String toAvatar) {
        this.toAvatar = toAvatar;
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

    public String getTypeText() {
        return typeText;
    }

    public void setTypeText(String typeText) {
        this.typeText = typeText;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public Boolean getIsUnread() {
        return isUnread;
    }

    public void setIsUnread(Boolean isUnread) {
        this.isUnread = isUnread;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "id=" + id +
                ", fromUserId=" + fromUserId +
                ", fromUsername='" + fromUsername + '\'' +
                ", fromNickname='" + fromNickname + '\'' +
                ", fromAvatar='" + fromAvatar + '\'' +
                ", toUserId=" + toUserId +
                ", toUsername='" + toUsername + '\'' +
                ", toNickname='" + toNickname + '\'' +
                ", toAvatar='" + toAvatar + '\'' +
                ", productId=" + productId +
                ", productTitle='" + productTitle + '\'' +
                ", productImage='" + productImage + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", typeText='" + typeText + '\'' +
                ", isRead=" + isRead +
                ", createTime=" + createTime +
                ", readTime=" + readTime +
                ", isUnread=" + isUnread +
                ", readStatus='" + readStatus + '\'' +
                '}';
    }
}