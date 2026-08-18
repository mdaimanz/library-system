package com.library.management.service;

import com.library.management.api.dto.BookRequest;
import com.library.management.api.dto.BookResponse;
import com.library.management.domain.model.Book;
import com.library.management.domain.repository.BookRepository;
import com.library.management.domain.repository.BorrowerRepository;
import com.library.management.exception.InvalidIsbnFormatException;
import com.library.management.mapper.BookMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(BookServiceCachingTest.TestConfiguration.class)
class BookServiceCachingTest {

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

    @BeforeEach
    void resetTestState() {
        reset(bookRepository, borrowerRepository, bookMapper);
        cacheManager.getCache("books").clear();
    }

    @Test
    void findAllCachesTheMappedCatalogue() {
        Book book = Book.builder().id(UUID.randomUUID()).title("Cached Book").build();
        BookResponse response = new BookResponse(book.getId(), "9780306406157", "Cached Book", "Author");
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(bookMapper.toBookResponse(book)).thenReturn(response);

        assertEquals(List.of(response), bookService.findAll());
        assertEquals(List.of(response), bookService.findAll());

        verify(bookRepository).findAll();
        verify(bookMapper).toBookResponse(book);
    }

    @Test
    void successfulCreateEvictsTheCachedCatalogue() {
        Book existingBook = Book.builder().id(UUID.randomUUID()).title("Existing").build();
        BookResponse existingResponse = new BookResponse(
                existingBook.getId(), "9780306406157", "Existing", "Existing Author"
        );
        BookResponse createdResponse = new BookResponse(
                UUID.randomUUID(), "9780132350884", "Clean Code", "Robert Martin"
        );
        when(bookRepository.findAll()).thenReturn(List.of(existingBook));
        when(bookMapper.toBookResponse(existingBook)).thenReturn(existingResponse);
        when(bookMapper.toBookResponse(any(Book.class))).thenReturn(createdResponse);

        bookService.findAll();
        bookService.create(new BookRequest("9780132350884", "Clean Code", "Robert Martin"));
        bookService.findAll();

        verify(bookRepository, times(2)).findAll();
    }

    @Test
    void failedCreateKeepsTheCachedCatalogue() {
        Book existingBook = Book.builder().id(UUID.randomUUID()).title("Existing").build();
        BookResponse existingResponse = new BookResponse(
                existingBook.getId(), "9780306406157", "Existing", "Existing Author"
        );
        when(bookRepository.findAll()).thenReturn(List.of(existingBook));
        when(bookMapper.toBookResponse(existingBook)).thenReturn(existingResponse);

        bookService.findAll();
        assertThrows(
                InvalidIsbnFormatException.class,
                () -> bookService.create(new BookRequest("invalid", "Bad Book", "Valid Author"))
        );
        bookService.findAll();

        verify(bookRepository).findAll();
    }

    @Configuration
    @EnableCaching
    static class TestConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("books");
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
