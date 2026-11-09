package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.dto.CategoryRequest;
import com.example.campussystem.dto.CategoryResponse;
import com.example.campussystem.entity.Category;
import com.example.campussystem.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<Category> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(
                CategoryResponse.fromListWithoutChildren(categories), "获取分类列表成功"));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
        List<Category> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success(
                CategoryResponse.fromList(categoryTree), "获取分类树成功"));
    }

    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getTopLevelCategories() {
        List<Category> categories = categoryService.getTopLevelCategories();
        return ResponseEntity.ok(ApiResponse.success(
                CategoryResponse.fromList(categories), "获取顶级分类成功"));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoriesByParentId(@PathVariable Long parentId) {
        List<Category> categories = categoryService.getCategoriesByParentId(parentId);
        return ResponseEntity.ok(ApiResponse.success(
                CategoryResponse.fromList(categories), "获取子分类成功"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(CategoryResponse.from(category), "获取分类详情成功"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.createCategory(
                request.getName(), request.getParentId(), request.getSortOrder());
        return ResponseEntity.ok(ApiResponse.success(CategoryResponse.from(category), "创建分类成功"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.updateCategory(
                id, request.getName(), request.getParentId(), request.getSortOrder(), request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(CategoryResponse.from(category), "更新分类成功"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("删除分类成功"));
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> toggleCategoryStatus(@PathVariable Long id) {
        Category category = categoryService.toggleCategoryStatus(id);
        return ResponseEntity.ok(ApiResponse.success(CategoryResponse.from(category), "切换分类状态成功"));
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategoriesBatch(@RequestBody List<Long> ids) {
        categoryService.deleteCategoriesBatch(ids);
        return ResponseEntity.ok(ApiResponse.success("批量删除分类成功"));
    }

    @PostMapping("/init")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> initializeBasicCategories() {
        categoryService.initializeBasicCategories();
        return ResponseEntity.ok(ApiResponse.success("初始化基础分类数据成功"));
    }
}
