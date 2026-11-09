package com.example.campussystem.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品发布/更新请求DTO
 */
public class ProductRequest {

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "商品标题长度不能超过100个字符")
    private String title;

    @Size(max = 2000, message = "商品描述长度不能超过2000个字符")
    private String description;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    @DecimalMax(value = "99999999.99", message = "商品价格不能超过99999999.99")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "原价必须大于0")
    @DecimalMax(value = "99999999.99", message = "原价不能超过99999999.99")
    private BigDecimal originalPrice;

    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    @Size(max = 10, message = "最多只能上传10张图片")
    private List<String> images;

    // 商品详细信息字段
    @Size(max = 50, message = "品牌长度不能超过50个字符")
    private String brand;

    @Size(max = 50, message = "型号长度不能超过50个字符")
    private String model;

    @Size(max = 20, message = "颜色长度不能超过20个字符")
    private String color;

    @Size(max = 20, message = "尺寸长度不能超过20个字符")
    private String size;

    @Size(max = 50, message = "购买时间长度不能超过50个字符")
    private String purchaseTime;

    @Size(max = 20, message = "使用程度长度不能超过20个字符")
    private String condition; // 全新、九成新、八成新等

    @Size(max = 100, message = "购买地点长度不能超过100个字符")
    private String purchaseLocation;

    @Size(max = 500, message = "交易说明长度不能超过500个字符")
    private String tradeNotes; // 交易说明，如面交地点、注意事项等

    private Boolean isNegotiable = false; // 是否可议价

    private Boolean includeInvoice = false; // 是否包含发票

    private Boolean includeWarranty = false; // 是否在保修期内

    private List<String> tags;

    // 构造函数
    public ProductRequest() {}

    // Getter和Setter方法
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(String purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getPurchaseLocation() {
        return purchaseLocation;
    }

    public void setPurchaseLocation(String purchaseLocation) {
        this.purchaseLocation = purchaseLocation;
    }

    public String getTradeNotes() {
        return tradeNotes;
    }

    public void setTradeNotes(String tradeNotes) {
        this.tradeNotes = tradeNotes;
    }

    public Boolean getIsNegotiable() {
        return isNegotiable;
    }

    public void setIsNegotiable(Boolean isNegotiable) {
        this.isNegotiable = isNegotiable;
    }

    public Boolean getIncludeInvoice() {
        return includeInvoice;
    }

    public void setIncludeInvoice(Boolean includeInvoice) {
        this.includeInvoice = includeInvoice;
    }

    public Boolean getIncludeWarranty() {
        return includeWarranty;
    }

    public void setIncludeWarranty(Boolean includeWarranty) {
        this.includeWarranty = includeWarranty;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "ProductRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", originalPrice=" + originalPrice +
                ", categoryId=" + categoryId +
                ", images=" + images +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", color='" + color + '\'' +
                ", size='" + size + '\'' +
                ", purchaseTime='" + purchaseTime + '\'' +
                ", condition='" + condition + '\'' +
                ", purchaseLocation='" + purchaseLocation + '\'' +
                ", tradeNotes='" + tradeNotes + '\'' +
                ", isNegotiable=" + isNegotiable +
                ", includeInvoice=" + includeInvoice +
                ", includeWarranty=" + includeWarranty +
                ", tags='" + tags + '\'' +
                '}';
    }
}