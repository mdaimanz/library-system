package com.library.management.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.data.redis.RedisConnectionFailureException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisCacheErrorHandlerTest {

    private final RedisCacheErrorHandler errorHandler = new RedisCacheErrorHandler();
    private final Cache cache = namedCache();

    @Test
    void ignoresWrappedRedisConnectionFailures() {
        RuntimeException failure = new RuntimeException(
                "Cache operation failed",
                new RedisConnectionFailureException("Redis unavailable")
        );

        assertDoesNotThrow(() -> errorHandler.handleCacheGetError(failure, cache, "key"));
        assertDoesNotThrow(() -> errorHandler.handleCachePutError(failure, cache, "key", "value"));
        assertDoesNotThrow(() -> errorHandler.handleCacheEvictError(failure, cache, "key"));
        assertDoesNotThrow(() -> errorHandler.handleCacheClearError(failure, cache));
    }

    @Test
    void rethrowsNonConnectionCacheFailures() {
        RuntimeException failure = new IllegalStateException("Serialization failed");

        assertThrows(
                IllegalStateException.class,
                () -> errorHandler.handleCacheGetError(failure, cache, "key")
        );
    }

    private Cache namedCache() {
        Cache namedCache = mock(Cache.class);
        when(namedCache.getName()).thenReturn("books");
        return namedCache;
    }
}
