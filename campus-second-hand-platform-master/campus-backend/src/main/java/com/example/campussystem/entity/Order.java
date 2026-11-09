package com.example.campussystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "买家ID不能为空")
    private Long buyerId;

    @NotNull(message = "卖家ID不能为空")
    private Long sellerId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.01", message = "交易金额必须大于0")
    private BigDecimal amount;

    private Integer status = 1; // 1待确认,2进行中,3已完成,4已取消

    private Integer buyerConfirm = 0;

    private Integer sellerConfirm = 0;

    /** 交易方式: 1校内面交 2快递配送 */
    private Integer tradeType = 1;

    @Size(max = 50, message = "收货人姓名不能超过50个字符")
    private String receiverName;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String receiverPhone;

    @Size(max = 50, message = "校区长度不能超过50个字符")
    private String campus;

    @Size(max = 100, message = "楼栋信息不能超过100个字符")
    private String building;

    @Size(max = 200, message = "详细地址不能超过200个字符")
    private String addressDetail;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    @Size(max = 500, message = "取消原因长度不能超过500个字符")
    private String cancelReason;

    private LocalDateTime cancelTime;

    private LocalDateTime completeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 非数据库字段，仅用于关联加载
    @TableField(exist = false)
    private User buyer;

    @TableField(exist = false)
    private User seller;

    @TableField(exist = false)
    private Product product;

    public Order() {}

    public Order(String orderNo, Long buyerId, Long sellerId, Long productId, BigDecimal amount) {
        this.orderNo = orderNo;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.productId = productId;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getBuyerConfirm() { return buyerConfirm; }
    public void setBuyerConfirm(Integer buyerConfirm) { this.buyerConfirm = buyerConfirm; }

    public Integer getSellerConfirm() { return sellerConfirm; }
    public void setSellerConfirm(Integer sellerConfirm) { this.sellerConfirm = sellerConfirm; }

    public Integer getTradeType() { return tradeType; }
    public void setTradeType(Integer tradeType) { this.tradeType = tradeType; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getAddressDetail() { return addressDetail; }
    public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public LocalDateTime getCancelTime() { return cancelTime; }
    public void setCancelTime(LocalDateTime cancelTime) { this.cancelTime = cancelTime; }

    public LocalDateTime getCompleteTime() { return completeTime; }
    public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", buyerId=" + buyerId +
                ", sellerId=" + sellerId +
                ", productId=" + productId +
                ", amount=" + amount +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
