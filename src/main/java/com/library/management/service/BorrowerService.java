package com.library.management.service;

import com.library.management.api.dto.BorrowerRequest;
import com.library.management.api.dto.BorrowerResponse;
import com.library.management.domain.model.Borrower;
import com.library.management.domain.repository.BookRepository;
import com.library.management.domain.repository.BorrowerRepository;
import com.library.management.exception.DuplicateEmailException;
import com.library.management.mapper.BorrowerMapper;

import com.library.management.util.NameValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;
    private final BookRepository bookRepository;
    private final BorrowerMapper borrowerMapper;

    @Transactional
    public BorrowerResponse create(BorrowerRequest request) {
        NameValidator.validateName(request.name());
        validateEmail(request.email());

        Borrower borrower = Borrower.builder()
                .name(request.name())
                .emailAddress(request.email())
                .build();
        borrowerRepository.save(borrower);
        return borrowerMapper.toBorrowerResponse(borrower);
    }

    private void validateEmail(String emailAddress) {
        Optional<Borrower> borrower = borrowerRepository.findByEmailAddress(emailAddress);
        if(borrower.isPresent()) {
            throw new DuplicateEmailException("Email has been used");
        }
    }
}
