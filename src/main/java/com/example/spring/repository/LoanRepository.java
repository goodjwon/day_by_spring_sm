package com.example.spring.repository;

import com.example.spring.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT l FROM Loan l WHERE l.member.id = :memberId")
    List<Loan> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId")
    List<Loan> findByBookId(@Param("bookId") Long bookId);

    @Query("SELECT l FROM Loan l WHERE l.member.id = :memberId AND l.returnDate IS NULL")
    List<Loan> findByMemberIdAndReturnDateIsNull(@Param("memberId") Long memberId);

    List<Loan> findByBookIdAndReturnDateIsNull();

    List<Loan> findOverdueLoans(LocalDateTime currentDate);

    List<Loan> findByReturnDateIsNull();

    List<Loan> findByLoanDateBetween();

    boolean existsByBookIdAndReturnDateIsNull();

    long countOverdueLoans();

    default List<Loan> findOverdueLoans() {
        return findOverdueLoans(LocalDateTime.now());
    }

    default Loan findLoanById(Long id) {
        return findById(id).orElse(null);
    }
}
