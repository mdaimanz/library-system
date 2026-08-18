package com.library.management.service;

import com.library.management.api.dto.BookRequest;
import com.library.management.api.dto.BookResponse;
import com.library.management.api.dto.BorrowBookRequest;
import com.library.management.api.dto.BorrowBookResponse;
import com.library.management.api.dto.ReturnBookRequest;
import com.library.management.api.dto.ReturnBookResponse;
import com.library.management.domain.model.Book;
import com.library.management.domain.model.Borrower;
import com.library.management.domain.repository.BookRepository;
import com.library.management.domain.repository.BorrowerRepository;
import com.library.management.exception.InvalidBookException;
import com.library.management.exception.InvalidBorrowerException;
import com.library.management.exception.InvalidIsbnFormatException;
import com.library.management.exception.InvalidNameException;
import com.library.management.mapper.BookMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    @Test
    void findAllMapsBooksInRepositoryOrder() {
        Book firstBook = Book.builder().id(UUID.randomUUID()).title("First").build();
        Book secondBook = Book.builder().id(UUID.randomUUID()).title("Second").build();
        BookResponse firstResponse = new BookResponse(firstBook.getId(), "9780306406157", "First", "First Author");
        BookResponse secondResponse = new BookResponse(secondBook.getId(), "0306406152", "Second", "Second Author");
        when(bookRepository.findAll()).thenReturn(List.of(firstBook, secondBook));
        when(bookMapper.toBookResponse(firstBook)).thenReturn(firstResponse);
        when(bookMapper.toBookResponse(secondBook)).thenReturn(secondResponse);

        List<BookResponse> result = bookService.findAll();

        assertEquals(List.of(firstResponse, secondResponse), result);
        verify(bookMapper).toBookResponse(firstBook);
        verify(bookMapper).toBookResponse(secondBook);
    }

    @Test
    void findAllReturnsEmptyListWhenRepositoryIsEmpty() {
        when(bookRepository.findAll()).thenReturn(List.of());

        assertEquals(List.of(), bookService.findAll());

        verifyNoInteractions(bookMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0306406152",
            "0-306-40615-2",
            "0-8044-2957-X",
            "9780306406157",
            "978 0 306 40615 7"
    })
    void createSavesAndMapsBooksWithValidIsbnFormats(String isbn) {
        BookRequest request = new BookRequest(isbn, "A Book", "Valid Author");
        BookResponse expected = new BookResponse(UUID.randomUUID(), isbn, request.title(), request.author());
        when(bookMapper.toBookResponse(any(Book.class))).thenReturn(expected);

        BookResponse result = bookService.create(request);

        assertSame(expected, result);
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        Book savedBook = captor.getValue();
        assertEquals(isbn, savedBook.getIsbnNumber());
        assertEquals(request.title(), savedBook.getTitle());
        assertEquals(request.author(), savedBook.getAuthor());
        verify(bookMapper).toBookResponse(savedBook);
        verifyNoInteractions(borrowerRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "12345",
            "ABCDEFGHIJ",
            "0-306-40615-3",
            "978-0-306-40615-8",
            "978030640615X"
    })
    void createRejectsInvalidIsbnWithoutSideEffects(String isbn) {
        BookRequest request = new BookRequest(isbn, "A Book", "Valid Author");

        InvalidIsbnFormatException exception = assertThrows(
                InvalidIsbnFormatException.class,
                () -> bookService.create(request)
        );

        assertEquals("Wrong ISBN format", exception.getMessage());
        verifyNoInteractions(bookRepository, borrowerRepository, bookMapper);
    }

    @Test
    void createRejectsInvalidAuthorWithoutSavingOrMapping() {
        BookRequest request = new BookRequest("9780306406157", "A Book", "Author 123");

        InvalidNameException exception = assertThrows(
                InvalidNameException.class,
                () -> bookService.create(request)
        );

        assertEquals("Name must contain only letters and spaces", exception.getMessage());
        verifyNoInteractions(bookRepository, borrowerRepository, bookMapper);
    }

    @Test
    void borrowReturnsSuccessWhenRepositoryUpdatesBook() {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        Borrower borrower = Borrower.builder().id(borrowerId).build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(Book.builder().id(bookId).build()));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(bookRepository.updateBorrower(bookId, borrower)).thenReturn(1);

        BorrowBookResponse response = bookService.borrow(bookId, new BorrowBookRequest(borrowerId));

        assertEquals("SUCCESS", response.status());
        assertEquals("Book %s is now borrowed by %s".formatted(bookId, borrowerId), response.description());
        verify(bookRepository).updateBorrower(bookId, borrower);
    }

    @Test
    void borrowReturnsFailureWhenBookIsAlreadyBorrowed() {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        Borrower borrower = Borrower.builder().id(borrowerId).build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(Book.builder().id(bookId).build()));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(bookRepository.updateBorrower(bookId, borrower)).thenReturn(0);

        BorrowBookResponse response = bookService.borrow(bookId, new BorrowBookRequest(borrowerId));

        assertEquals("FAILED", response.status());
        assertEquals("Book %s is currently borrowed by someone else".formatted(bookId), response.description());
        verify(bookRepository).updateBorrower(bookId, borrower);
    }

    @Test
    void borrowRejectsMissingBookBeforeLookingUpBorrower() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        InvalidBookException exception = assertThrows(
                InvalidBookException.class,
                () -> bookService.borrow(bookId, new BorrowBookRequest(UUID.randomUUID()))
        );

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository, never()).updateBorrower(any(), any());
        verifyNoInteractions(borrowerRepository, bookMapper);
    }

    @Test
    void borrowRejectsMissingBorrowerWithoutUpdatingBook() {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(Book.builder().id(bookId).build()));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.empty());

        InvalidBorrowerException exception = assertThrows(
                InvalidBorrowerException.class,
                () -> bookService.borrow(bookId, new BorrowBookRequest(borrowerId))
        );

        assertEquals("Borrower not found", exception.getMessage());
        verify(bookRepository, never()).updateBorrower(any(), any());
        verifyNoInteractions(bookMapper);
    }

    @Test
    void returnBookReturnsSuccessWhenRepositoryClearsBorrower() {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        Borrower borrower = Borrower.builder().id(borrowerId).build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(Book.builder().id(bookId).build()));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(bookRepository.returnBook(bookId, borrower)).thenReturn(1);

        ReturnBookResponse response = bookService.returnBook(bookId, new ReturnBookRequest(borrowerId));

        assertEquals("SUCCESS", response.status());
        assertEquals("Book %s is successfully returned".formatted(bookId), response.description());
        verify(bookRepository).returnBook(bookId, borrower);
    }

    @Test
    void returnBookReturnsFailureWhenRepositoryCannotClearBorrower() {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        Borrower borrower = Borrower.builder().id(borrowerId).build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(Book.builder().id(bookId).build()));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(bookRepository.returnBook(bookId, borrower)).thenReturn(0);

        ReturnBookResponse response = bookService.returnBook(bookId, new ReturnBookRequest(borrowerId));

        assertEquals("FAILED", response.status());
        assertEquals("Return process failed", response.description());
        verify(bookRepository).returnBook(bookId, borrower);
    }

    @Test
    void returnBookRejectsMissingBookBeforeLookingUpBorrower() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        InvalidBookException exception = assertThrows(
                InvalidBookException.class,
                () -> bookService.returnBook(bookId, new ReturnBookRequest(UUID.randomUUID()))
        );

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository, never()).returnBook(any(), any());
        verifyNoInteractions(borrowerRepository, bookMapper);
    }

    @Test
    void returnBookRejectsMissingBorrowerWithoutUpdatingBook() {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(Book.builder().id(bookId).build()));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.empty());

        InvalidBorrowerException exception = assertThrows(
                InvalidBorrowerException.class,
                () -> bookService.returnBook(bookId, new ReturnBookRequest(borrowerId))
        );

        assertEquals("Borrower not found", exception.getMessage());
        verify(bookRepository, never()).returnBook(any(), any());
        verifyNoInteractions(bookMapper);
    }
}
