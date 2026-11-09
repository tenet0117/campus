package com.example.campussystem.service.impl;

import com.example.campussystem.service.FileUploadService;
import com.example.campussystem.service.ImageCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 图片缓存服务（Redis 可选）
 */
@Service
public class ImageCacheServiceImpl implements ImageCacheService {

    private static final Logger logger = LoggerFactory.getLogger(ImageCacheServiceImpl.class);

    private static final String CACHE_PREFIX = "image_cache:";
    private static final String METADATA_PREFIX = "image_meta:";
    private static final long DEFAULT_CACHE_DURATION = 24;

    @Autowired
    private ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider;

    @Autowired
    private FileUploadService fileUploadService;

    public void cacheImageUrl(String objectName, String imageUrl) {
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        try {
            String key = CACHE_PREFIX + objectName;
            redisTemplate.opsForValue().set(key, imageUrl, DEFAULT_CACHE_DURATION, TimeUnit.HOURS);
            logger.debug("缓存图片URL: {} -> {}", objectName, imageUrl);
        } catch (Exception e) {
            logger.warn("缓存图片URL失败: {}", objectName, e);
        }
    }

    public String getCachedImageUrl(String objectName) {
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return null;
        }
        try {
            String key = CACHE_PREFIX + objectName;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                logger.debug("命中缓存图片URL: {}", objectName);
                return cached.toString();
            }
        } catch (Exception e) {
            logger.warn("获取缓存图片URL失败: {}", objectName, e);
        }
        return null;
    }

    public void cacheImageMetadata(String objectName, Object metadata) {
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        try {
            String key = METADATA_PREFIX + objectName;
            redisTemplate.opsForValue().set(key, metadata, DEFAULT_CACHE_DURATION, TimeUnit.HOURS);
            logger.debug("缓存图片元数据: {}", objectName);
        } catch (Exception e) {
            logger.warn("缓存图片元数据失败: {}", objectName, e);
        }
    }

    public Object getCachedImageMetadata(String objectName) {
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return null;
        }
        try {
            String key = METADATA_PREFIX + objectName;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                logger.debug("命中缓存图片元数据: {}", objectName);
                return cached;
            }
        } catch (Exception e) {
            logger.warn("获取缓存图片元数据失败: {}", objectName, e);
        }
        return null;
    }

    public void deleteImageCache(String objectName) {
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.delete(CACHE_PREFIX + objectName);
            redisTemplate.delete(METADATA_PREFIX + objectName);
            logger.debug("删除图片缓存: {}", objectName);
        } catch (Exception e) {
            logger.warn("删除图片缓存失败: {}", objectName, e);
        }
    }

    @Override
    public void warmupCache(java.util.List<String> objectNames) {
        if (objectNames == null || objectNames.isEmpty()) {
            return;
        }
        if (redisTemplateProvider.getIfAvailable() == null) {
            return;
        }

        logger.info("开始预热图片缓存，数量: {}", objectNames.size());
        objectNames.parallelStream().forEach(objectName -> {
            try {
                if (getCachedImageUrl(objectName) == null) {
                    cacheImageUrl(objectName, fileUploadService.getFileUrl(objectName));
                }
            } catch (Exception e) {
                logger.warn("预热缓存失败: {}", objectName, e);
            }
        });
        logger.info("图片缓存预热完成");
    }
}
