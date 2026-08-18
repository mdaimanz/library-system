package com.library.management.api.exception;

import com.library.management.exception.DuplicateEmailException;
import com.library.management.exception.InvalidBookException;
import com.library.management.exception.InvalidBorrowerException;
import com.library.management.exception.InvalidEmailFormatException;
import com.library.management.exception.InvalidNameException;
import com.library.management.exception.InvalidIsbnFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidNameException.class)
    public ProblemDetail handleInvalidBorrowerName(InvalidNameException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
        problemDetail.setTitle("Invalid borrower name");
        return problemDetail;
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
        problemDetail.setTitle("Duplicate email");
        return problemDetail;
    }

    @ExceptionHandler(InvalidEmailFormatException.class)
    public ProblemDetail handleInvalidEmailFormat(InvalidEmailFormatException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
        problemDetail.setTitle("Invalid email format");
        return problemDetail;
    }

    @ExceptionHandler(InvalidBookException.class)
    public ProblemDetail handleInvalidBook(InvalidBookException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
        problemDetail.setTitle("Book not found");
        return problemDetail;
    }

    @ExceptionHandler(InvalidBorrowerException.class)
    public ProblemDetail handleInvalidBorrower(InvalidBorrowerException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
        problemDetail.setTitle("Borrower not found");
        return problemDetail;
    }

    @ExceptionHandler(InvalidIsbnFormatException.class)
    public ProblemDetail handleInvalidIsbnFormat(InvalidIsbnFormatException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST
                , exception.getMessage()
        );
        problemDetail.setTitle("Invalid ISBN format");
        return problemDetail;
    }
}
