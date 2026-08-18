package com.library.management.domain.repository;

import com.library.management.domain.model.Book;
import com.library.management.domain.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Book b SET b.borrowerId = :borrower
            WHERE b.id = :bookId
            AND b.borrowerId IS NULL
            """)
    int updateBorrower(@Param("bookId") UUID bookId, @Param("borrower") Borrower borrower);

    @Modifying
    @Query("""
            UPDATE Book b SET b.borrowerId = NULL
            WHERE b.id = :bookId
            AND b.borrowerId = :borrower
            """)
    int returnBook(UUID bookId, Borrower borrower);
}
