package com.example.campussystem.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface CacheService {

    void set(String key, Object value);

    void set(String key, Object value, long timeout, TimeUnit unit);

    Object get(String key);

    <T> T get(String key, Class<T> clazz);

    Boolean delete(String key);

    Long delete(Collection<String> keys);

    Boolean hasKey(String key);

    Boolean expire(String key, long timeout, TimeUnit unit);

    Long getExpire(String key);

    Set<String> keys(String pattern);

    void deleteByPattern(String pattern);

    Long leftPush(String key, Object value);

    Long rightPush(String key, Object value);

    Object leftPop(String key);

    Object rightPop(String key);

    List<Object> range(String key, long start, long end);

    Long listSize(String key);

    void hSet(String key, String hashKey, Object value);

    Object hGet(String key, String hashKey);

    Long hDelete(String key, Object... hashKeys);

    Boolean hHasKey(String key, String hashKey);

    Long sAdd(String key, Object... values);

    Long sRemove(String key, Object... values);

    Boolean sIsMember(String key, Object value);

    Set<Object> sMembers(String key);

    Boolean zAdd(String key, Object value, double score);

    Long zRemove(String key, Object... values);

    Set<Object> zRange(String key, long start, long end);

    Set<Object> zReverseRange(String key, long start, long end);
}
