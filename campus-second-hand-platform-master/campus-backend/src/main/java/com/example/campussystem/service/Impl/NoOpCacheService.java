package com.example.campussystem.service.impl;

import com.example.campussystem.service.CacheService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 未启用时的空实现：业务照常运行，仅跳过缓存
 */
@Service
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false")
public class NoOpCacheService implements CacheService {

    @Override
    public void set(String key, Object value) {
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return null;
    }

    @Override
    public Boolean delete(String key) {
        return false;
    }

    @Override
    public Long delete(Collection<String> keys) {
        return 0L;
    }

    @Override
    public Boolean hasKey(String key) {
        return false;
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return false;
    }

    @Override
    public Long getExpire(String key) {
        return -1L;
    }

    @Override
    public Set<String> keys(String pattern) {
        return Collections.emptySet();
    }

    @Override
    public void deleteByPattern(String pattern) {
    }

    @Override
    public Long leftPush(String key, Object value) {
        return 0L;
    }

    @Override
    public Long rightPush(String key, Object value) {
        return 0L;
    }

    @Override
    public Object leftPop(String key) {
        return null;
    }

    @Override
    public Object rightPop(String key) {
        return null;
    }

    @Override
    public List<Object> range(String key, long start, long end) {
        return Collections.emptyList();
    }

    @Override
    public Long listSize(String key) {
        return 0L;
    }

    @Override
    public void hSet(String key, String hashKey, Object value) {
    }

    @Override
    public Object hGet(String key, String hashKey) {
        return null;
    }

    @Override
    public Long hDelete(String key, Object... hashKeys) {
        return 0L;
    }

    @Override
    public Boolean hHasKey(String key, String hashKey) {
        return false;
    }

    @Override
    public Long sAdd(String key, Object... values) {
        return 0L;
    }

    @Override
    public Long sRemove(String key, Object... values) {
        return 0L;
    }

    @Override
    public Boolean sIsMember(String key, Object value) {
        return false;
    }

    @Override
    public Set<Object> sMembers(String key) {
        return Collections.emptySet();
    }

    @Override
    public Boolean zAdd(String key, Object value, double score) {
        return false;
    }

    @Override
    public Long zRemove(String key, Object... values) {
        return 0L;
    }

    @Override
    public Set<Object> zRange(String key, long start, long end) {
        return Collections.emptySet();
    }

    @Override
    public Set<Object> zReverseRange(String key, long start, long end) {
        return Collections.emptySet();
    }
}
