package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.dto.ProductRequest;
import com.example.campussystem.dto.ProductResponse;
import com.example.campussystem.security.UserPrincipal;
import com.example.campussystem.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 发布商品
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> publishProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        ProductResponse response = productService.publishProduct(request, userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "商品发布成功"));
    }

    /**
     * 保存商品草稿
     */
    @PostMapping("/draft")
    public ResponseEntity<ApiResponse<ProductResponse>> saveDraft(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        ProductResponse response = productService.saveDraft(request, userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "草稿保存成功"));
    }

    /**
     * 更新商品信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        ProductResponse response = productService.updateProduct(id, request, userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "商品更新成功"));
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductDetail(@PathVariable Long id) {
        ProductResponse response = productService.getProductDetail(id);
        return ResponseEntity.ok(ApiResponse.success(response, "获取商品详情成功"));
    }

    /**
     * 增加商品浏览次数
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<Void>> incrementViewCount(@PathVariable Long id) {
        productService.incrementViewCount(id);
        return ResponseEntity.ok(ApiResponse.success("浏览次数更新成功"));
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean hard,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (hard) {
            productService.hardDeleteProduct(id, userPrincipal.getUserId());
            return ResponseEntity.ok(ApiResponse.success("商品已彻底删除"));
        }
        productService.deleteProduct(id, userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("商品已下架"));
    }

    /**
     * 更新商品状态
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateProductStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        productService.updateProductStatus(id, status, userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("商品状态更新成功"));
    }

    /**
     * 获取商品列表（分页）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<ProductResponse> response = productService.getProductList(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response, "获取商品列表成功"));
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<ProductResponse> response = productService.searchProducts(
                keyword, categoryId, minPrice, maxPrice, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response, "搜索商品成功"));
    }

    /**
     * 根据分类获取商品列表
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<ProductResponse> response = productService.getProductsByCategory(categoryId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response, "获取分类商品成功"));
    }

    /**
     * 获取用户发布的商品列表（支持分页和筛选）
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getUserProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String title,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        // 【调试日志】记录当前用户信息，用于排查权限问题
        Page<ProductResponse> response = productService.getUserProductsWithPagination(
                userPrincipal.getUserId(), page, size, status, title);
        
        return ResponseEntity.ok(ApiResponse.success(response, "获取我的商品成功"));
    }

    /**
     * 获取指定用户的商品列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getUserProducts(@PathVariable Long userId) {
        List<ProductResponse> response = productService.getUserProducts(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "获取用户商品成功"));
    }

    /**
     * 获取热门商品
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getPopularProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ProductResponse> response = productService.getPopularProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "获取热门商品成功"));
    }

    /**
     * 获取最新商品
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getLatestProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ProductResponse> response = productService.getLatestProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "获取最新商品成功"));
    }

    /**
     * 获取推荐商品
     */
    @GetMapping("/{id}/recommendations")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getRecommendedProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<ProductResponse> response = productService.getRecommendedProducts(id, limit);
        return ResponseEntity.ok(ApiResponse.success(response, "获取推荐商品成功"));
    }

    /**
     * 统计用户商品数量
     */
    @GetMapping("/count/user/{userId}")
    public ResponseEntity<ApiResponse<Long>> countUserProducts(@PathVariable Long userId) {
        long count = productService.countUserProducts(userId);
        return ResponseEntity.ok(ApiResponse.success(count, "获取用户商品数量成功"));
    }

    /**
     * 统计分类商品数量
     */
    @GetMapping("/count/category/{categoryId}")
    public ResponseEntity<ApiResponse<Long>> countCategoryProducts(@PathVariable Long categoryId) {
        long count = productService.countCategoryProducts(categoryId);
        return ResponseEntity.ok(ApiResponse.success(count, "获取分类商品数量成功"));
    }
}