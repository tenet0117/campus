package com.example.campussystem.service;

public interface ImageCacheService {

    void cacheImageUrl(String objectName, String imageUrl);

    String getCachedImageUrl(String objectName);

    void cacheImageMetadata(String objectName, Object metadata);

    Object getCachedImageMetadata(String objectName);

    void deleteImageCache(String objectName);

    void warmupCache(java.util.List<String> objectNames);
}
