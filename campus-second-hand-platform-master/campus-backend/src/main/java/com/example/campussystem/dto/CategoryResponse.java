package com.example.campussystem.dto;

import com.example.campussystem.entity.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类响应DTO
 */
public class CategoryResponse {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<CategoryResponse> children;
    private Long productCount;

    // 构造函数
    public CategoryResponse() {}

    /**
     * 包含子分类的构造函数（用于分类树等需要完整层级结构的场景）
     */
    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.parentId = category.getParentId();
        this.sortOrder = category.getSortOrder();
        this.status = category.getStatus();
        this.createTime = category.getCreateTime();
        this.updateTime = category.getUpdateTime();
        
        // 转换子分类（此处会访问懒加载的 children，要求在有 Session 的事务中调用）
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            this.children = category.getChildren().stream()
                    .map(CategoryResponse::new)
                    .collect(Collectors.toList());
        }
        
        // 商品数量设为0，避免懒加载问题
        this.productCount = 0L;
    }

    // 静态工厂方法 - 默认包含子分类（用于树结构）
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category);
    }

    public static List<CategoryResponse> fromList(List<Category> categories) {
        return categories.stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 仅转换当前分类，不访问 children，避免懒加载异常。
     * 用于产品列表等只需要扁平分类列表的接口（/categories）。
     */
    public static CategoryResponse fromWithoutChildren(Category category) {
        CategoryResponse resp = new CategoryResponse();
        resp.id = category.getId();
        resp.name = category.getName();
        resp.parentId = category.getParentId();
        resp.sortOrder = category.getSortOrder();
        resp.status = category.getStatus();
        resp.createTime = category.getCreateTime();
        resp.updateTime = category.getUpdateTime();
        // 不设置 children，避免在 Session 之外触发懒加载
        resp.productCount = 0L;
        return resp;
    }

    public static List<CategoryResponse> fromListWithoutChildren(List<Category> categories) {
        return categories.stream()
                .map(CategoryResponse::fromWithoutChildren)
                .collect(Collectors.toList());
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public List<CategoryResponse> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryResponse> children) {
        this.children = children;
    }

    public Long getProductCount() {
        return productCount;
    }

    public void setProductCount(Long productCount) {
        this.productCount = productCount;
    }

    @Override
    public String toString() {
        return "CategoryResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", sortOrder=" + sortOrder +
                ", status=" + status +
                ", createTime=" + createTime +
                ", productCount=" + productCount +
                '}';
    }
}