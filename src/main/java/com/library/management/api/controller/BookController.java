package com.library.management.api.controller;

import com.library.management.api.dto.*;
import com.library.management.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public List<BookResponse> getAll() {
        return bookService.findAll();
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{bookId}/borrow")
    public ResponseEntity<BorrowBookResponse> borrow(@PathVariable UUID bookId, @Valid @RequestBody BorrowBookRequest request) {
        BorrowBookResponse response = bookService.borrow(bookId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{bookId}/return")
    public ResponseEntity<ReturnBookResponse> returnBook(@PathVariable UUID bookId, @Valid @RequestBody ReturnBookRequest request) {
        ReturnBookResponse response = bookService.returnBook(bookId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
