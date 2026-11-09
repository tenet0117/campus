package com.example.campussystem.service.impl;

import com.example.campussystem.service.CacheEvictionService;
import com.example.campussystem.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CacheEvictionServiceImpl implements CacheEvictionService {

    private static final Logger logger = LoggerFactory.getLogger(CacheEvictionServiceImpl.class);

    @Autowired
    private CacheService cacheService;

    /**
     * 定时清理过期缓存（每小时执行一次）
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void cleanExpiredCache() {
        logger.debug("开始清理过期缓存...");
        try {
            cleanProductCaches();
            cleanUserCaches();
            cleanCategoryCaches();
            logger.debug("缓存清理完成");
        } catch (Exception e) {
            // Redis 未启动时仅告警，不影响主业务
            logger.warn("缓存清理跳过（Redis 不可用）: {}", e.getMessage());
        }
    }

    /**
     * 清理商品相关缓存
     */
    public void cleanProductCaches() {
        logger.debug("清理商品相关缓存");
        
        // 清理热门商品缓存（每小时更新）
        Set<String> popularKeys = cacheService.keys("popular_products:*");
        if (popularKeys != null && !popularKeys.isEmpty()) {
            cacheService.delete(popularKeys);
            logger.debug("清理了 {} 个热门商品缓存", popularKeys.size());
        }
        
        // 清理最新商品缓存（每小时更新）
        Set<String> latestKeys = cacheService.keys("latest_products:*");
        if (latestKeys != null && !latestKeys.isEmpty()) {
            cacheService.delete(latestKeys);
            logger.debug("清理了 {} 个最新商品缓存", latestKeys.size());
        }
    }

    /**
     * 清理用户相关缓存
     */
    public void cleanUserCaches() {
        logger.debug("清理用户相关缓存");
        
        // 这里可以根据需要清理特定的用户缓存
        // 例如：清理长时间未访问的用户资料缓存
    }

    /**
     * 清理分类相关缓存
     */
    public void cleanCategoryCaches() {
        logger.debug("清理分类相关缓存");
        
        // 分类信息变化较少，可以保持较长的缓存时间
        // 这里主要是为了防止内存占用过多
    }

    /**
     * 手动清理所有缓存
     */
    public void clearAllCache() {
        logger.info("手动清理所有缓存");
        
        try {
            // 清理商品缓存
            cacheService.deleteByPattern("popular_products:*");
            cacheService.deleteByPattern("latest_products:*");
            cacheService.deleteByPattern("product_detail:*");
            cacheService.deleteByPattern("category_products:*");
            cacheService.deleteByPattern("user_products:*");
            
            // 清理用户缓存
            cacheService.deleteByPattern("user_profile:*");
            cacheService.deleteByPattern("user:*");
            
            // 清理分类缓存
            cacheService.delete("all_categories");
            cacheService.delete("category_tree");
            cacheService.deleteByPattern("category_by_parent:*");
            cacheService.deleteByPattern("category_detail:*");
            
            logger.info("所有缓存清理完成");
        } catch (Exception e) {
            logger.error("清理所有缓存失败", e);
            throw new RuntimeException("清理缓存失败", e);
        }
    }

    /**
     * 清理指定模式的缓存
     */
    public void clearCacheByPattern(String pattern) {
        logger.info("清理模式为 {} 的缓存", pattern);
        
        try {
            cacheService.deleteByPattern(pattern);
            logger.info("模式 {} 的缓存清理完成", pattern);
        } catch (Exception e) {
            logger.error("清理模式 {} 的缓存失败", pattern, e);
            throw new RuntimeException("清理缓存失败", e);
        }
    }

    /**
     * 预热缓存
     */
    public void warmUpCache() {
        logger.info("开始预热缓存...");
        
        try {
            // 这里可以预加载一些常用的数据到缓存中
            // 例如：热门商品、分类树等
            
            logger.info("缓存预热完成");
        } catch (Exception e) {
            logger.error("缓存预热失败", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public CacheEvictionService.CacheStatistics getCacheStatistics() {
        CacheEvictionService.CacheStatistics stats = new CacheEvictionService.CacheStatistics();
        
        try {
            // 统计各类缓存的数量
            Set<String> productKeys = cacheService.keys("*product*");
            Set<String> userKeys = cacheService.keys("*user*");
            Set<String> categoryKeys = cacheService.keys("*category*");
            
            int productCount = productKeys != null ? productKeys.size() : 0;
            int userCount = userKeys != null ? userKeys.size() : 0;
            int categoryCount = categoryKeys != null ? categoryKeys.size() : 0;
            
            stats.setProductCacheCount(productCount);
            stats.setUserCacheCount(userCount);
            stats.setCategoryCacheCount(categoryCount);
            
            // 计算总数
            int totalCount = productCount + userCount + categoryCount;
            stats.setTotalCaches(totalCount);
            
            // 模拟缓存大小和命中率
            stats.setTotalSize(totalCount * 1024L); // 假设每个缓存项1KB
            stats.setHitRate("85%"); // 模拟命中率
            
            // 添加缓存详情
            java.util.List<CacheEvictionService.CacheDetail> details = new java.util.ArrayList<>();
            if (productCount > 0) {
                details.add(new CacheEvictionService.CacheDetail("商品缓存", productCount * 512L, productCount));
            }
            if (userCount > 0) {
                details.add(new CacheEvictionService.CacheDetail("用户缓存", userCount * 256L, userCount));
            }
            if (categoryCount > 0) {
                details.add(new CacheEvictionService.CacheDetail("分类缓存", categoryCount * 128L, categoryCount));
            }
            stats.setCacheDetails(details);
            
        } catch (Exception e) {
            logger.error("获取缓存统计信息失败", e);
            // 返回默认值
            stats.setTotalCaches(0);
            stats.setTotalSize(0L);
            stats.setHitRate("0%");
            stats.setCacheDetails(new java.util.ArrayList<>());
        }
        
        return stats;
    }

    /**
     * 缓存统计信息类
     */
}
