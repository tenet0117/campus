package com.example.campussystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campussystem.dto.ProductRequest;
import com.example.campussystem.dto.ProductResponse;
import com.example.campussystem.entity.Category;
import com.example.campussystem.entity.Product;
import com.example.campussystem.entity.User;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.mapper.CategoryMapper;
import com.example.campussystem.mapper.ProductMapper;
import com.example.campussystem.mapper.UserMapper;
import com.example.campussystem.service.ProductService;
import com.example.campussystem.service.OrderService;
import com.example.campussystem.service.CacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheService cacheService;

    @Autowired
    @Lazy
    private OrderService orderService;

    private static final String CACHE_KEY_POPULAR_PRODUCTS = "popular_products:";
    private static final String CACHE_KEY_LATEST_PRODUCTS = "latest_products:";
    private static final String CACHE_KEY_PRODUCT_DETAIL = "product_detail:";
    private static final String CACHE_KEY_CATEGORY_PRODUCTS = "category_products:";
    private static final String CACHE_KEY_USER_PRODUCTS = "user_products:";

    public ProductResponse publishProduct(ProductRequest request, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        Category category = categoryMapper.selectById(request.getCategoryId());
        if (category == null) throw new BusinessException("商品分类不存在");

        Product product = new Product();
        product.setUserId(userId);
        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStatus(1);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                product.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片数据格式错误");
            }
        } else {
            product.setImages("[]");
        }

        product.setBrand(request.getBrand());
        product.setModel(request.getModel());
        product.setColor(request.getColor());
        product.setProductSize(request.getSize());
        product.setPurchaseTime(request.getPurchaseTime());
        product.setCondition(request.getCondition());
        product.setPurchaseLocation(request.getPurchaseLocation());
        product.setTradeNotes(request.getTradeNotes());
        product.setIsNegotiable(request.getIsNegotiable());
        product.setIncludeInvoice(request.getIncludeInvoice());
        product.setIncludeWarranty(request.getIncludeWarranty());
        product.setTags(request.getTags() != null ? String.join(",", request.getTags()) : null);

        productMapper.insert(product);
        clearProductCaches(product.getCategoryId(), userId);

        product.setUser(user);
        product.setCategory(category);
        return new ProductResponse(product);
    }

    public ProductResponse saveDraft(ProductRequest request, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        Category category = categoryMapper.selectById(request.getCategoryId());
        if (category == null) throw new BusinessException("商品分类不存在");

        Product product = new Product();
        product.setUserId(userId);
        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStatus(1);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                product.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片数据格式错误");
            }
        } else {
            product.setImages("[]");
        }

        product.setBrand(request.getBrand());
        product.setModel(request.getModel());
        product.setColor(request.getColor());
        product.setProductSize(request.getSize());
        product.setPurchaseTime(request.getPurchaseTime());
        product.setCondition(request.getCondition());
        product.setPurchaseLocation(request.getPurchaseLocation());
        product.setTradeNotes(request.getTradeNotes());
        product.setIsNegotiable(request.getIsNegotiable());
        product.setIncludeInvoice(request.getIncludeInvoice());
        product.setIncludeWarranty(request.getIncludeWarranty());
        product.setTags(request.getTags() != null ? String.join(",", request.getTags()) : null);

        productMapper.insert(product);
        clearProductCaches(product.getCategoryId(), userId);

        product.setUser(user);
        product.setCategory(category);
        return new ProductResponse(product);
    }

    public ProductResponse updateProduct(Long productId, ProductRequest request, Long userId) {
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, productId)
                .eq(Product::getUserId, userId));
        if (product == null) throw new BusinessException("商品不存在或无权限修改");

        if (!request.getCategoryId().equals(product.getCategoryId())) {
            if (categoryMapper.selectById(request.getCategoryId()) == null) {
                throw new BusinessException("商品分类不存在");
            }
        }

        Long oldCategoryId = product.getCategoryId();
        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());

        if (request.getImages() != null) {
            try {
                product.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片数据格式错误");
            }
        }

        product.setBrand(request.getBrand());
        product.setModel(request.getModel());
        product.setColor(request.getColor());
        product.setProductSize(request.getSize());
        product.setPurchaseTime(request.getPurchaseTime());
        product.setCondition(request.getCondition());
        product.setPurchaseLocation(request.getPurchaseLocation());
        product.setTradeNotes(request.getTradeNotes());
        product.setIsNegotiable(request.getIsNegotiable());
        product.setIncludeInvoice(request.getIncludeInvoice());
        product.setIncludeWarranty(request.getIncludeWarranty());
        product.setTags(request.getTags() != null ? String.join(",", request.getTags()) : null);

        productMapper.updateById(product);
        clearProductCaches(oldCategoryId, userId);
        if (!oldCategoryId.equals(request.getCategoryId())) {
            clearProductCaches(request.getCategoryId(), userId);
        }
        cacheService.delete(CACHE_KEY_PRODUCT_DETAIL + productId);

        return new ProductResponse(product);
    }

    public ProductResponse getProductDetail(Long productId) {
        String cacheKey = CACHE_KEY_PRODUCT_DETAIL + productId;
        ProductResponse cachedProduct = cacheService.get(cacheKey, ProductResponse.class);
        if (cachedProduct != null) {
            incrementViewCountAsync(productId);
            return cachedProduct;
        }

        Product product = productMapper.selectById(productId);
        if (product == null) throw new BusinessException("商品不存在");

        User user = userMapper.selectById(product.getUserId());
        if (user != null) product.setUser(user);

        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) product.setCategory(category);

        product.incrementViewCount();
        productMapper.updateById(product);

        ProductResponse response = new ProductResponse(product);
        cacheService.set(cacheKey, response, 30, java.util.concurrent.TimeUnit.MINUTES);
        return response;
    }

    private void clearProductCaches(Long categoryId, Long userId) {
        cacheService.deleteByPattern(CACHE_KEY_CATEGORY_PRODUCTS + categoryId + ":*");
        cacheService.delete(CACHE_KEY_USER_PRODUCTS + userId);
        cacheService.deleteByPattern(CACHE_KEY_POPULAR_PRODUCTS + "*");
        cacheService.deleteByPattern(CACHE_KEY_LATEST_PRODUCTS + "*");
    }

    public void incrementViewCount(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.incrementViewCount();
            productMapper.updateById(product);
            cacheService.delete(CACHE_KEY_PRODUCT_DETAIL + productId);
        }
    }

    private void incrementViewCountAsync(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.incrementViewCount();
            productMapper.updateById(product);
            cacheService.delete(CACHE_KEY_PRODUCT_DETAIL + productId);
        }
    }

    public void deleteProduct(Long productId, Long userId) {
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, productId)
                .eq(Product::getUserId, userId));
        if (product == null) throw new BusinessException("商品不存在或无权限删除");

        product.setStatus(0);
        productMapper.updateById(product);
        clearProductCaches(product.getCategoryId(), userId);
        cacheService.delete(CACHE_KEY_PRODUCT_DETAIL + productId);
    }

    public void hardDeleteProduct(Long productId, Long userId) {
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, productId)
                .eq(Product::getUserId, userId));
        if (product == null) throw new BusinessException("商品不存在或无权限删除");

        productMapper.deleteById(productId);
        clearProductCaches(product.getCategoryId(), userId);
        cacheService.delete(CACHE_KEY_PRODUCT_DETAIL + productId);
    }

    public void updateProductStatus(Long productId, Integer status, Long userId) {
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, productId)
                .eq(Product::getUserId, userId));
        if (product == null) throw new BusinessException("商品不存在或无权限修改");

        if (status < 0 || status > 3) throw new BusinessException("商品状态值无效");

        Integer oldStatus = product.getStatus();
        // 已预订 → 在售：必须先取消未完结订单，避免孤儿订单
        if (Integer.valueOf(2).equals(oldStatus) && Integer.valueOf(1).equals(status)) {
            orderService.cancelOpenOrdersForProduct(productId, userId, "卖家取消预约");
            clearProductCaches(product.getCategoryId(), userId);
            cacheService.delete(CACHE_KEY_PRODUCT_DETAIL + productId);
            return;
        }
        // 已售出不允许直接改回在售（应走新发布或后台处理）
        if (Integer.valueOf(3).equals(oldStatus) && Integer.valueOf(1).equals(status)) {
            throw new BusinessException("已售出商品不能直接上架，请重新发布");
        }

        product.setStatus(status);
        productMapper.updateById(product);
        clearProductCaches(product.getCategoryId(), userId);
        cacheService.delete(CACHE_KEY_PRODUCT_DETAIL + productId);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductList(int page, int size, String sortBy, String sortDir) {
        Page<Product> mpPage = new Page<>(page, size);
        Page<Product> products = productMapper.selectPage(mpPage, new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderBy(true, "ASC".equalsIgnoreCase(sortDir), getSortFunc(sortBy)));

        Page<ProductResponse> result = new Page<>(products.getCurrent(), products.getSize(), products.getTotal());
        result.setRecords(products.getRecords().stream().map(product -> {
            User user = userMapper.selectById(product.getUserId());
            if (user != null) product.setUser(user);
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) product.setCategory(category);
            return new ProductResponse(product);
        }).collect(Collectors.toList()));
        return result;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Long categoryId,
                                              BigDecimal minPrice, BigDecimal maxPrice,
                                              int page, int size, String sortBy, String sortDir) {
        Page<Product> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1);

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Product::getTitle, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }

        wrapper.orderBy(true, "ASC".equalsIgnoreCase(sortDir), getSortFunc(sortBy));

        Page<Product> products = productMapper.selectPage(mpPage, wrapper);

        Page<ProductResponse> result = new Page<>(products.getCurrent(), products.getSize(), products.getTotal());
        result.setRecords(products.getRecords().stream().map(product -> {
            User user = userMapper.selectById(product.getUserId());
            if (user != null) product.setUser(user);
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) product.setCategory(category);
            return new ProductResponse(product);
        }).collect(Collectors.toList()));
        return result;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDir) {
        String cacheKey = CACHE_KEY_CATEGORY_PRODUCTS + categoryId + ":" + page + ":" + size + ":" + sortBy + ":" + sortDir;

        @SuppressWarnings("unchecked")
        Page<ProductResponse> cached = cacheService.get(cacheKey, Page.class);
        if (cached != null) return cached;

        Page<Product> mpPage = new Page<>(page, size);
        Page<Product> products = productMapper.selectPage(mpPage, new LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, categoryId)
                .eq(Product::getStatus, 1)
                .orderBy(true, "ASC".equalsIgnoreCase(sortDir), getSortFunc(sortBy)));

        Page<ProductResponse> result = new Page<>(products.getCurrent(), products.getSize(), products.getTotal());
        result.setRecords(products.getRecords().stream().map(product -> {
            User user = userMapper.selectById(product.getUserId());
            if (user != null) product.setUser(user);
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) product.setCategory(category);
            return new ProductResponse(product);
        }).collect(Collectors.toList()));

        cacheService.set(cacheKey, result, 20, java.util.concurrent.TimeUnit.MINUTES);
        return result;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getUserProductsWithPagination(Long userId, int page, int size, Integer status, String title) {
        Page<Product> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, userId)
                .orderByDesc(Product::getCreateTime);

        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        } else {
            wrapper.ne(Product::getStatus, 0);
        }
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(Product::getTitle, title.trim());
        }

        Page<Product> products = productMapper.selectPage(mpPage, wrapper);

        Page<ProductResponse> result = new Page<>(products.getCurrent(), products.getSize(), products.getTotal());
        result.setRecords(products.getRecords().stream().map(product -> {
            User user = userMapper.selectById(product.getUserId());
            if (user != null) product.setUser(user);
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) product.setCategory(category);
            return new ProductResponse(product);
        }).collect(Collectors.toList()));
        return result;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getUserProducts(Long userId) {
        String cacheKey = CACHE_KEY_USER_PRODUCTS + userId;
        @SuppressWarnings("unchecked")
        List<ProductResponse> cached = cacheService.get(cacheKey, List.class);
        if (cached != null) return cached;

        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, userId)
                .ne(Product::getStatus, 0));

        List<ProductResponse> result = products.stream()
                .map(product -> {
                    User user = userMapper.selectById(product.getUserId());
                    if (user != null) product.setUser(user);
                    Category category = categoryMapper.selectById(product.getCategoryId());
                    if (category != null) product.setCategory(category);
                    return new ProductResponse(product);
                })
                .collect(Collectors.toList());

        cacheService.set(cacheKey, result, 10, java.util.concurrent.TimeUnit.MINUTES);
        return result;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getPopularProducts(int page, int size) {
        String cacheKey = CACHE_KEY_POPULAR_PRODUCTS + page + ":" + size;
        @SuppressWarnings("unchecked")
        Page<ProductResponse> cached = cacheService.get(cacheKey, Page.class);
        if (cached != null) return cached;

        Page<Product> mpPage = new Page<>(page, size);
        Page<Product> products = productMapper.selectPage(mpPage, new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getViewCount));

        Page<ProductResponse> result = new Page<>(products.getCurrent(), products.getSize(), products.getTotal());
        result.setRecords(products.getRecords().stream().map(ProductResponse::new).collect(Collectors.toList()));
        cacheService.set(cacheKey, result, 15, java.util.concurrent.TimeUnit.MINUTES);
        return result;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getLatestProducts(int page, int size) {
        String cacheKey = CACHE_KEY_LATEST_PRODUCTS + page + ":" + size;
        @SuppressWarnings("unchecked")
        Page<ProductResponse> cached = cacheService.get(cacheKey, Page.class);
        if (cached != null) return cached;

        Page<Product> mpPage = new Page<>(page, size);
        Page<Product> products = productMapper.selectPage(mpPage, new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreateTime));

        Page<ProductResponse> result = new Page<>(products.getCurrent(), products.getSize(), products.getTotal());
        result.setRecords(products.getRecords().stream().map(product -> {
            User user = userMapper.selectById(product.getUserId());
            if (user != null) product.setUser(user);
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) product.setCategory(category);
            return new ProductResponse(product);
        }).collect(Collectors.toList()));

        cacheService.set(cacheKey, result, 10, java.util.concurrent.TimeUnit.MINUTES);
        return result;
    }

    private SFunction<Product, ?> getSortFunc(String sortBy) {
        switch (sortBy) {
            case "price": return Product::getPrice;
            case "viewCount": return Product::getViewCount;
            default: return Product::getCreateTime;
        }
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getRecommendedProducts(Long userId, int limit) {
        Page<Product> mpPage = new Page<>(0, limit);
        Page<Product> products = productMapper.selectPage(mpPage, new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getViewCount));
        return products.getRecords().stream().map(ProductResponse::new).collect(Collectors.toList());
    }

    public long countUserProducts(Long userId) {
        return productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getUserId, userId));
    }

    public long countCategoryProducts(Long categoryId) {
        return productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, categoryId));
    }
}
