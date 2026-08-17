package com.library.management.service;

import com.library.management.api.dto.BorrowerRequest;
import com.library.management.api.dto.BorrowerResponse;
import com.library.management.domain.model.Borrower;
import com.library.management.domain.repository.BorrowerRepository;
import com.library.management.exception.DuplicateEmailException;
import com.library.management.exception.InvalidBorrowerNameException;
import com.library.management.mapper.BorrowerMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BorrowerService {

    private static final Pattern VALID_NAME_PATTERN =
            Pattern.compile("^\\p{L}[\\p{L}\\p{M}]*(?: \\p{L}[\\p{L}\\p{M}]*)*$");

    private final BorrowerRepository borrowerRepository;
    private final BorrowerMapper borrowerMapper;

    @Transactional
    public BorrowerResponse create(BorrowerRequest request) {
        validateName(request.name());
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

    private void validateName(String name) {
        if (name == null || name.isBlank() || !VALID_NAME_PATTERN.matcher(name).matches()) {
            throw new InvalidBorrowerNameException("Name must contain only letters and spaces");
        }
    }
}
