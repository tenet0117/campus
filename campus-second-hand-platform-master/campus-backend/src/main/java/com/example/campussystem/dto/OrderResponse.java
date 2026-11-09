package com.example.campussystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单响应DTO
 */
public class OrderResponse {

    private Long id;
    private String orderNo;
    private Long buyerId;
    private String buyerUsername;
    private String buyerNickname;
    private Long sellerId;
    private String sellerUsername;
    private String sellerNickname;
    private Long productId;
    private String productTitle;
    private String productImage;
    private BigDecimal amount;
    private Integer status;
    private String statusText;
    private Integer buyerConfirm;
    private Integer sellerConfirm;
    private Integer tradeType;
    private String tradeTypeText;
    private String receiverName;
    private String receiverPhone;
    private String campus;
    private String building;
    private String addressDetail;
    private String remark;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 商品详细信息
    private BigDecimal productPrice;
    private String productDescription;
    private String productCategory;
    
    // 买家详细信息
    private String buyerPhone;
    private String buyerEmail;
    
    // 卖家详细信息
    private String sellerPhone;
    private String sellerEmail;

    // 构造函数
    public OrderResponse() {}

    public OrderResponse(Long id, String orderNo, Long buyerId, String buyerUsername, String buyerNickname,
                        Long sellerId, String sellerUsername, String sellerNickname,
                        Long productId, String productTitle, String productImage,
                        BigDecimal amount, Integer status, String statusText,
                        Integer buyerConfirm, Integer sellerConfirm, String remark,
                        LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.orderNo = orderNo;
        this.buyerId = buyerId;
        this.buyerUsername = buyerUsername;
        this.buyerNickname = buyerNickname;
        this.sellerId = sellerId;
        this.sellerUsername = sellerUsername;
        this.sellerNickname = sellerNickname;
        this.productId = productId;
        this.productTitle = productTitle;
        this.productImage = productImage;
        this.amount = amount;
        this.status = status;
        this.statusText = statusText;
        this.buyerConfirm = buyerConfirm;
        this.sellerConfirm = sellerConfirm;
        this.remark = remark;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerUsername() {
        return buyerUsername;
    }

    public void setBuyerUsername(String buyerUsername) {
        this.buyerUsername = buyerUsername;
    }

    public String getBuyerNickname() {
        return buyerNickname;
    }

    public void setBuyerNickname(String buyerNickname) {
        this.buyerNickname = buyerNickname;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public String getSellerNickname() {
        return sellerNickname;
    }

    public void setSellerNickname(String sellerNickname) {
        this.sellerNickname = sellerNickname;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Integer getBuyerConfirm() {
        return buyerConfirm;
    }

    public void setBuyerConfirm(Integer buyerConfirm) {
        this.buyerConfirm = buyerConfirm;
    }

    public Integer getSellerConfirm() {
        return sellerConfirm;
    }

    public void setSellerConfirm(Integer sellerConfirm) {
        this.sellerConfirm = sellerConfirm;
    }

    public Integer getTradeType() {
        return tradeType;
    }

    public void setTradeType(Integer tradeType) {
        this.tradeType = tradeType;
    }

    public String getTradeTypeText() {
        return tradeTypeText;
    }

    public void setTradeTypeText(String tradeTypeText) {
        this.tradeTypeText = tradeTypeText;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getBuyerPhone() {
        return buyerPhone;
    }

    public void setBuyerPhone(String buyerPhone) {
        this.buyerPhone = buyerPhone;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getSellerPhone() {
        return sellerPhone;
    }

    public void setSellerPhone(String sellerPhone) {
        this.sellerPhone = sellerPhone;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", buyerId=" + buyerId +
                ", buyerUsername='" + buyerUsername + '\'' +
                ", buyerNickname='" + buyerNickname + '\'' +
                ", sellerId=" + sellerId +
                ", sellerUsername='" + sellerUsername + '\'' +
                ", sellerNickname='" + sellerNickname + '\'' +
                ", productId=" + productId +
                ", productTitle='" + productTitle + '\'' +
                ", productImage='" + productImage + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                ", statusText='" + statusText + '\'' +
                ", buyerConfirm=" + buyerConfirm +
                ", sellerConfirm=" + sellerConfirm +
                ", remark='" + remark + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}