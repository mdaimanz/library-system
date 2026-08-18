package com.library.management.service;

import com.library.management.api.dto.BorrowerRequest;
import com.library.management.api.dto.BorrowerResponse;
import com.library.management.domain.model.Borrower;
import com.library.management.domain.repository.BookRepository;
import com.library.management.domain.repository.BorrowerRepository;
import com.library.management.exception.DuplicateEmailException;
import com.library.management.exception.InvalidEmailFormatException;
import com.library.management.exception.InvalidNameException;
import com.library.management.mapper.BorrowerMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowerMapper borrowerMapper;

    @InjectMocks
    private BorrowerService borrowerService;

    @Test
    void createSavesAndMapsBorrowerWhenNameAndEmailAreAvailable() {
        BorrowerRequest request = new BorrowerRequest("Valid Borrower", "borrower@example.com");
        BorrowerResponse expected = new BorrowerResponse(UUID.randomUUID(), request.name(), request.email());
        when(borrowerRepository.findByEmailAddress(request.email())).thenReturn(Optional.empty());
        when(borrowerMapper.toBorrowerResponse(any(Borrower.class))).thenReturn(expected);

        BorrowerResponse result = borrowerService.create(request);

        assertSame(expected, result);
        ArgumentCaptor<Borrower> captor = ArgumentCaptor.forClass(Borrower.class);
        verify(borrowerRepository).save(captor.capture());
        Borrower savedBorrower = captor.getValue();
        assertEquals(request.name(), savedBorrower.getName());
        assertEquals(request.email(), savedBorrower.getEmailAddress());
        verify(borrowerMapper).toBorrowerResponse(savedBorrower);
        verifyNoInteractions(bookRepository);
    }

    @Test
    void createRejectsInvalidNameBeforeAccessingRepositories() {
        BorrowerRequest request = new BorrowerRequest("Borrower 123", "borrower@example.com");

        InvalidNameException exception = assertThrows(
                InvalidNameException.class,
                () -> borrowerService.create(request)
        );

        assertEquals("Name must contain only letters and spaces", exception.getMessage());
        verifyNoInteractions(borrowerRepository, bookRepository, borrowerMapper);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "plainaddress",
            "@example.com",
            "borrower@",
            "borrower@example",
            "borrower..name@example.com",
            "borrower@example..com",
            "borrower name@example.com"
    })
    void createRejectsInvalidEmailBeforeAccessingRepositories(String email) {
        BorrowerRequest request = new BorrowerRequest("Valid Borrower", email);

        InvalidEmailFormatException exception = assertThrows(
                InvalidEmailFormatException.class,
                () -> borrowerService.create(request)
        );

        assertEquals("Invalid email format", exception.getMessage());
        verifyNoInteractions(borrowerRepository, bookRepository, borrowerMapper);
    }

    @Test
    void createRejectsDuplicateEmailWithoutSavingOrMapping() {
        BorrowerRequest request = new BorrowerRequest("Valid Borrower", "borrower@example.com");
        when(borrowerRepository.findByEmailAddress(request.email()))
                .thenReturn(Optional.of(Borrower.builder().id(UUID.randomUUID()).build()));

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> borrowerService.create(request)
        );

        assertEquals("Email has been used", exception.getMessage());
        verify(borrowerRepository, never()).save(any());
        verifyNoInteractions(bookRepository, borrowerMapper);
    }
}
