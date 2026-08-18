package com.library.management.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.RedisConnectionFailureException;

public class RedisCacheErrorHandler implements CacheErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        handleConnectionFailure(exception, cache, "read", key);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        handleConnectionFailure(exception, cache, "write", key);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        handleConnectionFailure(exception, cache, "evict", key);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        handleConnectionFailure(exception, cache, "clear", null);
    }

    private void handleConnectionFailure(
            RuntimeException exception,
            Cache cache,
            String operation,
            Object key) {
        if (!isRedisConnectionFailure(exception)) {
            throw exception;
        }

        LOGGER.warn(
                "Redis cache {} failed for cache '{}' and key '{}'; continuing without cache: {}",
                operation,
                cache.getName(),
                key,
                exception.getMessage()
        );
    }

    private boolean isRedisConnectionFailure(Throwable exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof RedisConnectionFailureException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
