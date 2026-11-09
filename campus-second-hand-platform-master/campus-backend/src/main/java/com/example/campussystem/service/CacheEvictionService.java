package com.example.campussystem.service;

import java.util.List;

public interface CacheEvictionService {

    void cleanExpiredCache();

    void cleanProductCaches();

    void cleanUserCaches();

    void cleanCategoryCaches();

    void clearAllCache();

    void clearCacheByPattern(String pattern);

    void warmUpCache();

    CacheStatistics getCacheStatistics();

    class CacheStatistics {
        private int productCacheCount;
        private int userCacheCount;
        private int categoryCacheCount;
        private int totalCaches;
        private long totalSize;
        private String hitRate;
        private List<CacheDetail> cacheDetails;

        public CacheStatistics() {
            this.totalCaches = 0;
            this.totalSize = 0L;
            this.hitRate = "0%";
            this.cacheDetails = new java.util.ArrayList<>();
        }

        public int getProductCacheCount() { return productCacheCount; }
        public void setProductCacheCount(int v) { this.productCacheCount = v; }
        public int getUserCacheCount() { return userCacheCount; }
        public void setUserCacheCount(int v) { this.userCacheCount = v; }
        public int getCategoryCacheCount() { return categoryCacheCount; }
        public void setCategoryCacheCount(int v) { this.categoryCacheCount = v; }
        public int getTotalCaches() { return totalCaches; }
        public void setTotalCaches(int v) { this.totalCaches = v; }
        public long getTotalSize() { return totalSize; }
        public void setTotalSize(long v) { this.totalSize = v; }
        public String getHitRate() { return hitRate; }
        public void setHitRate(String v) { this.hitRate = v; }
        public List<CacheDetail> getCacheDetails() { return cacheDetails; }
        public void setCacheDetails(List<CacheDetail> v) { this.cacheDetails = v; }
        public int getTotalCacheCount() { return totalCaches; }
        public void setTotalCacheCount(int v) { this.totalCaches = v; }
    }

    class CacheDetail {
        private String name;
        private long size;
        private int entries;

        public CacheDetail() {}
        public CacheDetail(String name, long size, int entries) {
            this.name = name; this.size = size; this.entries = entries;
        }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        public int getEntries() { return entries; }
        public void setEntries(int entries) { this.entries = entries; }
    }
}
