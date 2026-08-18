package com.library.management.service;

import com.library.management.api.dto.*;
import com.library.management.domain.model.Book;
import com.library.management.domain.model.Borrower;
import com.library.management.domain.repository.BookRepository;
import com.library.management.domain.repository.BorrowerRepository;
import com.library.management.exception.InvalidBookException;
import com.library.management.exception.InvalidBorrowerException;
import com.library.management.exception.InvalidIsbnFormatException;
import com.library.management.mapper.BookMapper;
import com.library.management.util.NameValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BorrowerRepository borrowerRepository;
    private final BookMapper bookMapper;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "books")
    public List<BookResponse> findAll() {
        return bookRepository
                .findAll()
                .stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(cacheNames = "books", allEntries = true)
    public BookResponse create(BookRequest request) {
        if(!isValidIsbn(request.isbnNumber())){
            throw new InvalidIsbnFormatException("Wrong ISBN format");
        }

        NameValidator.validateName(request.author());

        Book book = Book.builder()
                .isbnNumber(request.isbnNumber())
                .title(request.title())
                .author(request.author())
                .build();
        bookRepository.save(book);
        return bookMapper.toBookResponse(book);
    }

    @Transactional
    public BorrowBookResponse borrow(UUID bookId, BorrowBookRequest request) {
        //validate book & borrower
        isBookExist(bookId);
        Borrower borrower = getBorrower(request.borrowerId());

        //update
        int result = bookRepository.updateBorrower(bookId, borrower);

        if(result == 0){
            return new BorrowBookResponse("FAILED", "Book %s is currently borrowed by someone else"
                    .formatted(bookId));
        }

        return new BorrowBookResponse("SUCCESS", "Book %s is now borrowed by %s"
                .formatted(bookId, request.borrowerId()));
    }

    @Transactional
    public ReturnBookResponse returnBook(UUID bookId, ReturnBookRequest request){
        isBookExist(bookId);
        Borrower borrower = getBorrower(request.borrowerId());

        //update
        int result = bookRepository.returnBook(bookId, borrower);

        if(result == 0){
            return new ReturnBookResponse("FAILED", "Return process failed");
        }

        return new ReturnBookResponse("SUCCESS", "Book %s is successfully returned".formatted(bookId));
    }

    private void isBookExist(UUID bookId) {
        Optional<Book> book = bookRepository.findById(bookId);
        if(book.isEmpty()) {
            throw new InvalidBookException("Book not found");
        }
    }

    private Borrower getBorrower(UUID borrowerId) {
        return borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new InvalidBorrowerException("Borrower not found"));
    }


    //there are 2 ISBN format, ISBN-13 and ISBN-10
    private boolean isValidIsbn(String isbnNumber) {
        String normalized = isbnNumber
                .replace("-","")
                .replace(" ", "");
        return switch (normalized.length()) {
            case 10 -> isValidIsbn10Format(normalized);
            case 13 -> isValidIsbn13Format(normalized);
            default -> false;
        };
    }

    /**
     * Verify if the isbnNumber is in correct ISBN-10 format or not.
     * Checking the regex, then calculate the ISBN-10 checksum by multiplying each character
     * with decreasing weight. It will be considered valid if the sum % 11 == 0
     *
     * @param isbnNumber
     * @return boolean
     */
    private boolean isValidIsbn10Format(String isbnNumber) {
        // Check pattern
        if(!isbnNumber.matches("\\d{9}[\\dXx]")) {
            return false;
        }

        int sum = 0;

        for (int i = 0; i < 10; i++) {
            // Get character
            char c = isbnNumber.charAt(i);

            int value;

            //Check for X as the last digit, considered as 10
            if (i == 9 && (c == 'X' || c == 'x')) {
                value = 10;
            } else {
                value = Character.getNumericValue(c);
            }

            sum += value * (10-i);
        }

        return sum % 11 == 0;
    }

    /**
     * Verify if the isbnNumber is in correct ISBN-13 format or not.
     * Checking the regex, then calculate the checksum by multiplying each character
     * with alternating weight (1 / 3). Or can say we multiply by 1 for even number, multiply by 3 for odd number.
     * Then check the 13th digit of the ISBN matches the checksum calculated from the first 12 digits.
     *
     * @param isbnNumber
     * @return boolean
     */
    private boolean isValidIsbn13Format(String isbnNumber) {
        if(!isbnNumber.matches("\\d{13}")) {
            return false;
        }

        int sum = 0;

        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(isbnNumber.charAt(i));

            int weight;

            if (i % 2 == 0) {
                weight = 1;
            } else {
                weight = 3;
            }

            sum += digit * weight;
        }

        int expected = (10 - (sum % 10)) % 10;
        int actual = Character.getNumericValue(isbnNumber.charAt(12));

        return expected == actual;
    }

}
