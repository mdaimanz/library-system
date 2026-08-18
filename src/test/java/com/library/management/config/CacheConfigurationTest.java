package com.library.management.config;

import com.library.management.api.dto.BookResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(CacheManager.class, () -> new ConcurrentMapCacheManager("books"))
            .withBean(ObjectMapper.class, () -> JsonMapper.builder().findAndAddModules().build())
            .withUserConfiguration(CacheConfiguration.class);

    @Test
    void cacheTimeToLiveDefaultsToThirtyMinutes() {
        contextRunner.run(context -> assertEquals(
                Duration.ofMinutes(30),
                timeToLive(context.getBean(RedisCacheConfiguration.class))
        ));
    }

    @Test
    void cacheTimeToLiveCanBeOverridden() {
        contextRunner
                .withPropertyValues("spring.cache.redis.time-to-live=5m")
                .run(context -> assertEquals(
                        Duration.ofMinutes(5),
                        timeToLive(context.getBean(RedisCacheConfiguration.class))
                ));
    }

    @Test
    void cacheValuesUseJsonSerialization() {
        contextRunner.run(context -> {
            RedisCacheConfiguration configuration = context.getBean(RedisCacheConfiguration.class);
            List<BookResponse> catalogue = List.of(new BookResponse(
                    UUID.randomUUID(), "9780306406157", "Cached Book", "Author"
            ));

            Object restored = configuration.getValueSerializationPair().read(
                    configuration.getValueSerializationPair().write(catalogue)
            );

            assertEquals(catalogue, restored);
        });
    }

    private Duration timeToLive(RedisCacheConfiguration configuration) {
        return configuration.getTtlFunction().getTimeToLive(null, null);
    }
}
