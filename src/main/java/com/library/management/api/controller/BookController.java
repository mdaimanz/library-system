package com.library.management.api.controller;

import com.library.management.api.dto.*;
import com.library.management.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Manage the book catalogue and book loans")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "List books", description = "Returns every book currently in the catalogue.")
    @ApiResponse(
            responseCode = "200",
            description = "Catalogue returned",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = BookResponse.class)),
                    examples = {
                            @ExampleObject(name = "books", value = """
                                    [{"id":"7b7d1847-34b2-4cbd-a7e4-601a967446b0","isbnNumber":"9780306406157","title":"The Pragmatic Programmer","author":"David Thomas"}]
                                    """),
                            @ExampleObject(name = "emptyCatalogue", value = "[]")
                    }
            )
    )
    public List<BookResponse> getAll() {
        return bookService.findAll();
    }

    @PostMapping
    @Operation(
            summary = "Add a book",
            description = "Adds a book with a valid ISBN-10 or ISBN-13 to the catalogue."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Book added",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BookResponse.class),
                            examples = @ExampleObject(value = """
                                    {"id":"7b7d1847-34b2-4cbd-a7e4-601a967446b0","isbnNumber":"9780306406157","title":"The Pragmatic Programmer","author":"David Thomas"}
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request, ISBN, or author name",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = {
                                    @ExampleObject(name = "invalidIsbn", value = """
                                            {"type":"about:blank","title":"Invalid ISBN format","status":400,"detail":"Wrong ISBN format","instance":"/api/v1/books"}
                                            """),
                                    @ExampleObject(name = "invalidAuthor", value = """
                                            {"type":"about:blank","title":"Invalid borrower name","status":400,"detail":"Name must contain only letters and spaces","instance":"/api/v1/books"}
                                            """)
                            }
                    )
            )
    })
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{bookId}/borrow")
    @Operation(
            summary = "Borrow a book",
            description = "Assigns an available book to a borrower. An unavailable book produces a FAILED business outcome with HTTP 200."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Borrow attempt completed; inspect status for SUCCESS or FAILED",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BorrowBookResponse.class),
                            examples = {
                                    @ExampleObject(name = "success", value = """
                                            {"status":"SUCCESS","description":"Book 7b7d1847-34b2-4cbd-a7e4-601a967446b0 is now borrowed by 458015b3-8688-4b03-b6df-5579fe6e1296"}
                                            """),
                                    @ExampleObject(name = "alreadyBorrowed", value = """
                                            {"status":"FAILED","description":"Book 7b7d1847-34b2-4cbd-a7e4-601a967446b0 is currently borrowed by someone else"}
                                            """)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed book ID, missing borrower ID, or invalid request body",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book or borrower not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = {
                                    @ExampleObject(name = "bookNotFound", value = """
                                            {"type":"about:blank","title":"Book not found","status":404,"detail":"Book not found","instance":"/api/v1/books/7b7d1847-34b2-4cbd-a7e4-601a967446b0/borrow"}
                                            """),
                                    @ExampleObject(name = "borrowerNotFound", value = """
                                            {"type":"about:blank","title":"Borrower not found","status":404,"detail":"Borrower not found","instance":"/api/v1/books/7b7d1847-34b2-4cbd-a7e4-601a967446b0/borrow"}
                                            """)
                            }
                    )
            )
    })
    public ResponseEntity<BorrowBookResponse> borrow(
            @Parameter(description = "Unique book identifier", required = true, example = "7b7d1847-34b2-4cbd-a7e4-601a967446b0")
            @PathVariable UUID bookId,
            @Valid @RequestBody BorrowBookRequest request
    ) {
        BorrowBookResponse response = bookService.borrow(bookId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{bookId}/return")
    @Operation(
            summary = "Return a book",
            description = "Removes the borrower assignment from a book. An invalid return state produces a FAILED business outcome with HTTP 200."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Return attempt completed; inspect status for SUCCESS or FAILED",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ReturnBookResponse.class),
                            examples = {
                                    @ExampleObject(name = "success", value = """
                                            {"status":"SUCCESS","description":"Book 7b7d1847-34b2-4cbd-a7e4-601a967446b0 is successfully returned"}
                                            """),
                                    @ExampleObject(name = "failed", value = """
                                            {"status":"FAILED","description":"Return process failed"}
                                            """)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed book ID, missing borrower ID, or invalid request body",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book or borrower not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = {
                                    @ExampleObject(name = "bookNotFound", value = """
                                            {"type":"about:blank","title":"Book not found","status":404,"detail":"Book not found","instance":"/api/v1/books/7b7d1847-34b2-4cbd-a7e4-601a967446b0/return"}
                                            """),
                                    @ExampleObject(name = "borrowerNotFound", value = """
                                            {"type":"about:blank","title":"Borrower not found","status":404,"detail":"Borrower not found","instance":"/api/v1/books/7b7d1847-34b2-4cbd-a7e4-601a967446b0/return"}
                                            """)
                            }
                    )
            )
    })
    public ResponseEntity<ReturnBookResponse> returnBook(
            @Parameter(description = "Unique book identifier", required = true, example = "7b7d1847-34b2-4cbd-a7e4-601a967446b0")
            @PathVariable UUID bookId,
            @Valid @RequestBody ReturnBookRequest request
    ) {
        ReturnBookResponse response = bookService.returnBook(bookId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
