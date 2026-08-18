package com.library.management.service;

import com.library.management.api.dto.BorrowerRequest;
import com.library.management.api.dto.BorrowerResponse;
import com.library.management.domain.model.Borrower;
import com.library.management.domain.repository.BookRepository;
import com.library.management.domain.repository.BorrowerRepository;
import com.library.management.exception.DuplicateEmailException;
import com.library.management.exception.InvalidEmailFormatException;
import com.library.management.mapper.BorrowerMapper;

import com.library.management.util.NameValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BorrowerService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
                    + "@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}$"
    );

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
        if (emailAddress == null
                || emailAddress.length() > 254
                || !EMAIL_PATTERN.matcher(emailAddress).matches()) {
            throw new InvalidEmailFormatException("Invalid email format");
        }

        Optional<Borrower> borrower = borrowerRepository.findByEmailAddress(emailAddress);
        if(borrower.isPresent()) {
            throw new DuplicateEmailException("Email has been used");
        }
    }
}
