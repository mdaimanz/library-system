package com.library.management.config;

import com.library.management.api.dto.BookResponse;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfiguration implements CachingConfigurer {

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisCacheErrorHandler();
    }

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(
            CacheProperties cacheProperties,
            ObjectMapper objectMapper) {
        Duration configuredTimeToLive = cacheProperties.getRedis().getTimeToLive();
        Duration timeToLive = configuredTimeToLive != null
                ? configuredTimeToLive
                : Duration.ofMinutes(30);
        JavaType catalogueType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, BookResponse.class);
        RedisSerializer<Object> valueSerializer = new JacksonJsonRedisSerializer<>(objectMapper, catalogueType);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(timeToLive)
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer)
                );
    }
}
