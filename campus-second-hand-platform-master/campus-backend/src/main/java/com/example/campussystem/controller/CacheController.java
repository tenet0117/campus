package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.service.CacheEvictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 缓存管理控制器
 */
@RestController
@RequestMapping("/cache")
@PreAuthorize("hasRole('ADMIN')")
public class CacheController {

    @Autowired
    private CacheEvictionService cacheEvictionService;

    /**
     * 获取缓存信息 (前端调用的端点)
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<CacheEvictionService.CacheStatistics>> getCacheInfo() {
        CacheEvictionService.CacheStatistics statistics = cacheEvictionService.getCacheStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics, "获取缓存信息成功"));
    }

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<CacheEvictionService.CacheStatistics>> getCacheStatistics() {
        CacheEvictionService.CacheStatistics statistics = cacheEvictionService.getCacheStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics, "获取缓存统计信息成功"));
    }

    /**
     * 清理缓存 (前端调用的端点)
     */
    @PostMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCache(@RequestBody(required = false) String[] cacheNames) {
        if (cacheNames == null || cacheNames.length == 0) {
            // 清理所有缓存
            cacheEvictionService.clearAllCache();
            return ResponseEntity.ok(ApiResponse.success("所有缓存清理成功"));
        } else {
            // 根据缓存名称清理特定缓存
            for (String cacheName : cacheNames) {
                if ("expired".equals(cacheName)) {
                    cacheEvictionService.cleanExpiredCache();
                } else {
                    cacheEvictionService.clearCacheByPattern(cacheName);
                }
            }
            return ResponseEntity.ok(ApiResponse.success("指定缓存清理成功"));
        }
    }

    /**
     * 清理所有缓存
     */
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Void>> clearAllCache() {
        cacheEvictionService.clearAllCache();
        return ResponseEntity.ok(ApiResponse.success("所有缓存清理成功"));
    }

    /**
     * 清理商品相关缓存
     */
    @DeleteMapping("/products")
    public ResponseEntity<ApiResponse<Void>> clearProductCaches() {
        cacheEvictionService.cleanProductCaches();
        return ResponseEntity.ok(ApiResponse.success("商品缓存清理成功"));
    }

    /**
     * 清理用户相关缓存
     */
    @DeleteMapping("/users")
    public ResponseEntity<ApiResponse<Void>> clearUserCaches() {
        cacheEvictionService.cleanUserCaches();
        return ResponseEntity.ok(ApiResponse.success("用户缓存清理成功"));
    }

    /**
     * 清理分类相关缓存
     */
    @DeleteMapping("/categories")
    public ResponseEntity<ApiResponse<Void>> clearCategoryCaches() {
        cacheEvictionService.cleanCategoryCaches();
        return ResponseEntity.ok(ApiResponse.success("分类缓存清理成功"));
    }

    /**
     * 根据模式清理缓存
     */
    @DeleteMapping("/pattern")
    public ResponseEntity<ApiResponse<Void>> clearCacheByPattern(@RequestParam String pattern) {
        cacheEvictionService.clearCacheByPattern(pattern);
        return ResponseEntity.ok(ApiResponse.success("模式缓存清理成功"));
    }

    /**
     * 预热缓存
     */
    @PostMapping("/warmup")
    public ResponseEntity<ApiResponse<Void>> warmUpCache() {
        cacheEvictionService.warmUpCache();
        return ResponseEntity.ok(ApiResponse.success("缓存预热成功"));
    }

    /**
     * 手动触发缓存清理任务
     */
    @PostMapping("/clean")
    public ResponseEntity<ApiResponse<Void>> manualCleanCache() {
        cacheEvictionService.cleanExpiredCache();
        return ResponseEntity.ok(ApiResponse.success("手动缓存清理完成"));
    }
}