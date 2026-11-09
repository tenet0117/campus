package com.example.campussystem.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 订单请求DTO（含收货/联系信息）
 */
public class OrderRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 【已废弃】前端传递的金额字段，仅用于前端显示
     * 后端不再使用此字段，而是直接从商品表查询实际价格
     */
    @Deprecated
    private BigDecimal amount;

    /** 交易方式: 1校内面交 2快递配送 */
    @NotNull(message = "请选择交易方式")
    @Min(value = 1, message = "交易方式无效")
    @Max(value = 2, message = "交易方式无效")
    private Integer tradeType = 1;

    @NotBlank(message = "请填写收货人/联系人")
    @Size(max = 50, message = "收货人姓名不能超过50个字符")
    private String receiverName;

    @NotBlank(message = "请填写联系电话")
    @Pattern(regexp = "^1\\d{10}$|^0\\d{2,3}-?\\d{7,8}$", message = "联系电话格式不正确")
    private String receiverPhone;

    @Size(max = 50, message = "校区长度不能超过50个字符")
    private String campus;

    @Size(max = 100, message = "楼栋信息不能超过100个字符")
    private String building;

    @Size(max = 200, message = "详细地址不能超过200个字符")
    private String addressDetail;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    public OrderRequest() {}

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getTradeType() {
        return tradeType;
    }

    public void setTradeType(Integer tradeType) {
        this.tradeType = tradeType;
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
}
