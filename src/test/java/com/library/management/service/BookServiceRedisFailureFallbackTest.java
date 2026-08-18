package com.library.management.service;

import com.library.management.api.dto.BookRequest;
import com.library.management.api.dto.BookResponse;
import com.library.management.config.RedisCacheErrorHandler;
import com.library.management.domain.model.Book;
import com.library.management.domain.repository.BookRepository;
import com.library.management.domain.repository.BorrowerRepository;
import com.library.management.mapper.BookMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(BookServiceRedisFailureFallbackTest.TestConfiguration.class)
class BookServiceRedisFailureFallbackTest {

    private static final RedisConnectionFailureException REDIS_UNAVAILABLE =
            new RedisConnectionFailureException("Redis unavailable");

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private CacheManager cacheManager;

    private Cache cache;

    @BeforeEach
    void resetTestState() {
        cache = cacheManager.getCache("books");
        reset(bookRepository, borrowerRepository, bookMapper, cache);
        when(cache.getName()).thenReturn("books");
        when(cache.get(any())).thenThrow(REDIS_UNAVAILABLE);
        doThrow(REDIS_UNAVAILABLE).when(cache).put(any(), any());
        doThrow(REDIS_UNAVAILABLE).when(cache).clear();
    }

    @Test
    void findAllQueriesTheRepositoryWhenRedisIsUnavailable() {
        Book book = Book.builder().id(UUID.randomUUID()).title("Available Book").build();
        BookResponse response = new BookResponse(book.getId(), "9780306406157", "Available Book", "Author");
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(bookMapper.toBookResponse(book)).thenReturn(response);

        assertEquals(List.of(response), bookService.findAll());
        assertEquals(List.of(response), bookService.findAll());

        verify(bookRepository, times(2)).findAll();
    }

    @Test
    void createReturnsNormallyWhenCacheEvictionCannotReachRedis() {
        BookResponse response = new BookResponse(
                UUID.randomUUID(), "9780132350884", "Clean Code", "Robert Martin"
        );
        when(bookMapper.toBookResponse(any(Book.class))).thenReturn(response);

        BookResponse result = bookService.create(
                new BookRequest("9780132350884", "Clean Code", "Robert Martin")
        );

        assertSame(response, result);
        verify(bookRepository).save(any(Book.class));
        verify(cache).clear();
    }

    @Configuration
    @EnableCaching
    static class TestConfiguration implements CachingConfigurer {

        @Bean
        @Override
        public CacheErrorHandler errorHandler() {
            return new RedisCacheErrorHandler();
        }

        @Bean
        @Override
        public CacheManager cacheManager() {
            CacheManager cacheManager = mock(CacheManager.class);
            when(cacheManager.getCache("books")).thenReturn(mock(Cache.class));
            return cacheManager;
        }

        @Bean
        BookRepository bookRepository() {
            return mock(BookRepository.class);
        }

        @Bean
        BorrowerRepository borrowerRepository() {
            return mock(BorrowerRepository.class);
        }

        @Bean
        BookMapper bookMapper() {
            return mock(BookMapper.class);
        }

        @Bean
        BookService bookService(
                BookRepository bookRepository,
                BorrowerRepository borrowerRepository,
                BookMapper bookMapper) {
            return new BookService(bookRepository, borrowerRepository, bookMapper);
        }
    }
}
