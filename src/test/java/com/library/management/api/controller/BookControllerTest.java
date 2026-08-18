package com.library.management.api.controller;

import com.library.management.api.dto.BookRequest;
import com.library.management.api.dto.BookResponse;
import com.library.management.api.dto.BorrowBookRequest;
import com.library.management.api.dto.BorrowBookResponse;
import com.library.management.api.dto.ReturnBookRequest;
import com.library.management.api.dto.ReturnBookResponse;
import com.library.management.exception.InvalidBookException;
import com.library.management.exception.InvalidBorrowerException;
import com.library.management.exception.InvalidIsbnFormatException;
import com.library.management.exception.InvalidNameException;
import com.library.management.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @Test
    void getAllReturnsBooks() throws Exception {
        BookResponse first = new BookResponse(UUID.randomUUID(), "9780306406157", "First Book", "First Author");
        BookResponse second = new BookResponse(UUID.randomUUID(), "0306406152", "Second Book", "Second Author");
        when(bookService.findAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(first.id().toString()))
                .andExpect(jsonPath("$[0].isbnNumber").value(first.isbnNumber()))
                .andExpect(jsonPath("$[0].title").value(first.title()))
                .andExpect(jsonPath("$[0].author").value(first.author()))
                .andExpect(jsonPath("$[1].id").value(second.id().toString()));

        verify(bookService).findAll();
    }

    @Test
    void getAllReturnsEmptyArrayWhenThereAreNoBooks() throws Exception {
        when(bookService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(bookService).findAll();
    }

    @Test
    void createReturnsCreatedBookAndDelegatesRequest() throws Exception {
        BookRequest request = new BookRequest("9780306406157", "A Book", "Valid Author");
        BookResponse response = new BookResponse(UUID.randomUUID(), request.isbnNumber(), request.title(), request.author());
        when(bookService.create(any(BookRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.isbnNumber").value(response.isbnNumber()))
                .andExpect(jsonPath("$.title").value(response.title()))
                .andExpect(jsonPath("$.author").value(response.author()));

        verify(bookService).create(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"title\":\"A Book\",\"author\":\"Valid Author\"}",
            "{\"isbnNumber\":\"\",\"title\":\"A Book\",\"author\":\"Valid Author\"}",
            "{\"isbnNumber\":\"9780306406157\",\"author\":\"Valid Author\"}",
            "{\"isbnNumber\":\"9780306406157\",\"title\":\"\",\"author\":\"Valid Author\"}",
            "{\"isbnNumber\":\"9780306406157\",\"title\":\"A Book\"}",
            "{\"isbnNumber\":\"9780306406157\",\"title\":\"A Book\",\"author\":\"\"}"
    })
    void createRejectsMissingOrBlankFields(String body) throws Exception {
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void createMapsInvalidIsbnToProblemDetail() throws Exception {
        BookRequest request = new BookRequest("invalid", "A Book", "Valid Author");
        when(bookService.create(any(BookRequest.class)))
                .thenThrow(new InvalidIsbnFormatException("Wrong ISBN format"));

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid ISBN format"))
                .andExpect(jsonPath("$.detail").value("Wrong ISBN format"));
    }

    @Test
    void createMapsInvalidAuthorToProblemDetail() throws Exception {
        BookRequest request = new BookRequest("9780306406157", "A Book", "Author 123");
        when(bookService.create(any(BookRequest.class)))
                .thenThrow(new InvalidNameException("Name must contain only letters and spaces"));

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid borrower name"))
                .andExpect(jsonPath("$.detail").value("Name must contain only letters and spaces"));
    }

    @Test
    void borrowReturnsSuccessAndDelegatesRequest() throws Exception {
        UUID bookId = UUID.randomUUID();
        BorrowBookRequest request = new BorrowBookRequest(UUID.randomUUID());
        BorrowBookResponse response = new BorrowBookResponse("SUCCESS", "Borrowed");
        when(bookService.borrow(bookId, request)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/books/{bookId}/borrow", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.description").value("Borrowed"));

        verify(bookService).borrow(bookId, request);
    }

    @Test
    void borrowReturnsBusinessFailureWithOkHttpStatus() throws Exception {
        UUID bookId = UUID.randomUUID();
        BorrowBookRequest request = new BorrowBookRequest(UUID.randomUUID());
        BorrowBookResponse response = new BorrowBookResponse("FAILED", "Book is already borrowed");
        when(bookService.borrow(bookId, request)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/books/{bookId}/borrow", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.description").value("Book is already borrowed"));

        verify(bookService).borrow(bookId, request);
    }

    @Test
    void borrowRejectsMissingBorrowerIdWithoutCallingService() throws Exception {
        mockMvc.perform(patch("/api/v1/books/{bookId}/borrow", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void borrowRejectsMalformedBookIdWithoutCallingService() throws Exception {
        mockMvc.perform(patch("/api/v1/books/not-a-uuid/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"borrowerId\":\"%s\"}".formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void borrowMapsMissingBookToNotFoundProblemDetail() throws Exception {
        UUID bookId = UUID.randomUUID();
        BorrowBookRequest request = new BorrowBookRequest(UUID.randomUUID());
        when(bookService.borrow(bookId, request)).thenThrow(new InvalidBookException("Book not found"));

        performBorrow(bookId, request)
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Book not found"))
                .andExpect(jsonPath("$.detail").value("Book not found"));
    }

    @Test
    void borrowMapsMissingBorrowerToNotFoundProblemDetail() throws Exception {
        UUID bookId = UUID.randomUUID();
        BorrowBookRequest request = new BorrowBookRequest(UUID.randomUUID());
        when(bookService.borrow(bookId, request)).thenThrow(new InvalidBorrowerException("Borrower not found"));

        performBorrow(bookId, request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Borrower not found"))
                .andExpect(jsonPath("$.detail").value("Borrower not found"));
    }

    @Test
    void returnBookReturnsSuccessAndDelegatesRequest() throws Exception {
        UUID bookId = UUID.randomUUID();
        ReturnBookRequest request = new ReturnBookRequest(UUID.randomUUID());
        ReturnBookResponse response = new ReturnBookResponse("SUCCESS", "Returned");
        when(bookService.returnBook(bookId, request)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/books/{bookId}/return", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.description").value("Returned"));

        verify(bookService).returnBook(bookId, request);
    }

    @Test
    void returnBookReturnsBusinessFailureWithOkHttpStatus() throws Exception {
        UUID bookId = UUID.randomUUID();
        ReturnBookRequest request = new ReturnBookRequest(UUID.randomUUID());
        ReturnBookResponse response = new ReturnBookResponse("FAILED", "Return process failed");
        when(bookService.returnBook(bookId, request)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/books/{bookId}/return", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.description").value("Return process failed"));

        verify(bookService).returnBook(bookId, request);
    }

    @Test
    void returnBookRejectsMissingBorrowerIdWithoutCallingService() throws Exception {
        mockMvc.perform(patch("/api/v1/books/{bookId}/return", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void returnBookRejectsMalformedBookIdWithoutCallingService() throws Exception {
        mockMvc.perform(patch("/api/v1/books/not-a-uuid/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"borrowerId\":\"%s\"}".formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void returnBookMapsMissingBookToNotFoundProblemDetail() throws Exception {
        UUID bookId = UUID.randomUUID();
        ReturnBookRequest request = new ReturnBookRequest(UUID.randomUUID());
        when(bookService.returnBook(bookId, request)).thenThrow(new InvalidBookException("Book not found"));

        performReturn(bookId, request)
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Book not found"))
                .andExpect(jsonPath("$.detail").value("Book not found"));
    }

    @Test
    void returnBookMapsMissingBorrowerToNotFoundProblemDetail() throws Exception {
        UUID bookId = UUID.randomUUID();
        ReturnBookRequest request = new ReturnBookRequest(UUID.randomUUID());
        when(bookService.returnBook(bookId, request)).thenThrow(new InvalidBorrowerException("Borrower not found"));

        performReturn(bookId, request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Borrower not found"))
                .andExpect(jsonPath("$.detail").value("Borrower not found"));
    }

    private org.springframework.test.web.servlet.ResultActions performBorrow(
            UUID bookId,
            BorrowBookRequest request
    ) throws Exception {
        return mockMvc.perform(patch("/api/v1/books/{bookId}/borrow", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private org.springframework.test.web.servlet.ResultActions performReturn(
            UUID bookId,
            ReturnBookRequest request
    ) throws Exception {
        return mockMvc.perform(patch("/api/v1/books/{bookId}/return", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
