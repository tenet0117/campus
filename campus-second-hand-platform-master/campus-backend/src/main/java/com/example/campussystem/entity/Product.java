package com.example.campussystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@TableName("products")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

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

    private String images;

    private Integer viewCount = 0;

    private Integer status = 1; // 1上架,2已预订,3已售出,0已删除

    @Size(max = 50, message = "品牌长度不能超过50个字符")
    private String brand;

    @Size(max = 50, message = "型号长度不能超过50个字符")
    private String model;

    @Size(max = 20, message = "颜色长度不能超过20个字符")
    private String color;

    @Size(max = 20, message = "尺寸长度不能超过20个字符")
    @TableField("size")
    private String productSize;

    @Size(max = 50, message = "购买时间长度不能超过50个字符")
    private String purchaseTime;

    @Size(max = 20, message = "使用程度长度不能超过20个字符")
    @TableField("`condition`")
    private String condition; // 全新、九成新、八成新等

    @Size(max = 100, message = "购买地点长度不能超过100个字符")
    private String purchaseLocation;

    @Size(max = 500, message = "交易说明长度不能超过500个字符")
    private String tradeNotes;

    private Boolean isNegotiable = false;

    private Boolean includeInvoice = false;

    private Boolean includeWarranty = false;

    @Size(max = 100, message = "标签长度不能超过100个字符")
    private String tags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 非数据库字段，仅用于关联加载
    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Category category;

    // 构造函数
    public Product() {}

    public Product(Long userId, Long categoryId, String title, String description, BigDecimal price) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.price = price;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getProductSize() { return productSize; }
    public void setProductSize(String productSize) { this.productSize = productSize; }

    public String getPurchaseTime() { return purchaseTime; }
    public void setPurchaseTime(String purchaseTime) { this.purchaseTime = purchaseTime; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getPurchaseLocation() { return purchaseLocation; }
    public void setPurchaseLocation(String purchaseLocation) { this.purchaseLocation = purchaseLocation; }

    public String getTradeNotes() { return tradeNotes; }
    public void setTradeNotes(String tradeNotes) { this.tradeNotes = tradeNotes; }

    public Boolean getIsNegotiable() { return isNegotiable; }
    public void setIsNegotiable(Boolean isNegotiable) { this.isNegotiable = isNegotiable; }

    public Boolean getIncludeInvoice() { return includeInvoice; }
    public void setIncludeInvoice(Boolean includeInvoice) { this.includeInvoice = includeInvoice; }

    public Boolean getIncludeWarranty() { return includeWarranty; }
    public void setIncludeWarranty(Boolean includeWarranty) { this.includeWarranty = includeWarranty; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    // 业务方法
    public boolean isAvailable() { return status != null && status == 1; }

    public boolean isSold() { return status != null && status == 3; }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", userId=" + userId +
                ", categoryId=" + categoryId +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", originalPrice=" + originalPrice +
                ", viewCount=" + viewCount +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
