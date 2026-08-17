package com.library.management.api.exception;

import com.library.management.exception.DuplicateEmailException;
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
