package com.example.campussystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campussystem.dto.ProductRequest;
import com.example.campussystem.dto.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponse publishProduct(ProductRequest request, Long userId);

    ProductResponse saveDraft(ProductRequest request, Long userId);

    ProductResponse updateProduct(Long productId, ProductRequest request, Long userId);

    ProductResponse getProductDetail(Long productId);

    void incrementViewCount(Long productId);

    void deleteProduct(Long productId, Long userId);

    void hardDeleteProduct(Long productId, Long userId);

    void updateProductStatus(Long productId, Integer status, Long userId);

    Page<ProductResponse> getProductList(int page, int size, String sortBy, String sortDir);

    Page<ProductResponse> searchProducts(String keyword, Long categoryId,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         int page, int size, String sortBy, String sortDir);

    Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDir);

    Page<ProductResponse> getUserProductsWithPagination(Long userId, int page, int size, Integer status, String title);

    List<ProductResponse> getUserProducts(Long userId);

    Page<ProductResponse> getPopularProducts(int page, int size);

    Page<ProductResponse> getLatestProducts(int page, int size);

    List<ProductResponse> getRecommendedProducts(Long userId, int limit);

    long countUserProducts(Long userId);

    long countCategoryProducts(Long categoryId);
}
