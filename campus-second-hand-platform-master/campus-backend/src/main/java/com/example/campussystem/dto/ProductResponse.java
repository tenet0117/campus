package com.example.campussystem.dto;

import com.example.campussystem.entity.Product;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品响应DTO
 */
public class ProductResponse {

    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private String phone;
    private String email;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private List<String> images;
    private Integer viewCount;
    private Integer status;
    private String statusText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 商品详细信息字段
    private String brand;
    private String model;
    private String color;
    private String size;
    private String purchaseTime;
    private String condition;
    private String purchaseLocation;
    private String tradeNotes;
    private Boolean isNegotiable;
    private Boolean includeInvoice;
    private Boolean includeWarranty;
    private List<String> tags;

    // 构造函数
    public ProductResponse() {}

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.userId = product.getUserId();
        this.categoryId = product.getCategoryId();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.originalPrice = product.getOriginalPrice();
        this.viewCount = product.getViewCount();
        this.status = product.getStatus();
        this.createTime = product.getCreateTime();
        this.updateTime = product.getUpdateTime();
        
        // 设置状态文本
        this.statusText = getStatusText(product.getStatus());

        this.brand = product.getBrand();
        this.model = product.getModel();
        this.color = product.getColor();
        this.size = product.getProductSize();
        this.purchaseTime = product.getPurchaseTime();
        this.condition = product.getCondition();
        this.purchaseLocation = product.getPurchaseLocation();
        this.tradeNotes = product.getTradeNotes();
        this.isNegotiable = product.getIsNegotiable();
        this.includeInvoice = product.getIncludeInvoice();
        this.includeWarranty = product.getIncludeWarranty();
        if (product.getTags() != null && !product.getTags().isBlank()) {
            this.tags = java.util.Arrays.stream(product.getTags().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 处理图片JSON - 使用ObjectMapper正确解析
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                // 使用TypeReference来正确解析List<String>
                com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>> typeRef = 
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {};
                this.images = mapper.readValue(product.getImages(), typeRef);
            } catch (Exception e) {
                // 如果解析失败，尝试简单的字符串处理作为备选方案，兼容旧数据
                try {
                    String imagesStr = product.getImages();
                    if (imagesStr.startsWith("[") && imagesStr.endsWith("]")) {
                        // 移除首尾的方括号
                        imagesStr = imagesStr.substring(1, imagesStr.length() - 1);
                        // 按逗号分割并清理引号
                        String[] imageArray = imagesStr.split(",");
                        this.images = java.util.Arrays.stream(imageArray)
                            .map(String::trim)
                            .map(s -> s.replaceAll("^\"|\"$", "")) // 移除首尾引号
                            .collect(java.util.stream.Collectors.toList());
                    } else {
                        // 非JSON数组的情况，直接作为单个图片路径处理
                        this.images = List.of(imagesStr.trim());
                    }
                } catch (Exception ex) {
                    this.images = List.of();
                }
            }
        } else {
            this.images = List.of();
        }
        
        // 如果有关联的用户和分类信息，设置相关字段
        if (product.getUser() != null) {
            this.username = product.getUser().getUsername();
            this.userAvatar = product.getUser().getAvatar();
            this.phone = product.getUser().getPhone();
            this.email = product.getUser().getEmail();
        }
        
        if (product.getCategory() != null) {
            this.categoryName = product.getCategory().getName();
        }
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "已下架";
            case 1 -> "在售";
            case 2 -> "已预订";
            case 3 -> "已售出";
            default -> "未知";
        };
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

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

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
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
}