package com.library.management.domain.repository;

import com.library.management.domain.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BorrowerRepository extends JpaRepository<Borrower, Long> {

    Optional<Borrower> findByEmailAddress(String emailAddress);
}
