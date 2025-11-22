package com.example.spring.repository;

import com.example.spring.entity.Book;
import com.example.spring.entity.Loan;
import com.example.spring.entity.Member;
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

    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId AND l.returnDate IS NULL")
    List<Loan> findByBookIdAndReturnDateIsNull(@Param("bookId") Long bookId);

    @Query("SELECT l FROM Loan l WHERE l.returnDate IS NULL AND l.dueDate < :currentDate")
    List<Loan> findOverdueLoans(@Param("currentDate") LocalDateTime currentDate);

    List<Loan> findByReturnDateIsNull();

    @Query("SELECT l FROM Loan l WHERE l.loanDate BETWEEN :startDate AND :endDate")
    List<Loan> findByLoanDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Loan l WHERE l.book.id = :bookId AND l.returnDate IS NULL")
    boolean existsByBookIdAndReturnDateIsNull(@Param("bookId") Long bookId);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.returnDate IS NULL AND l.dueDate < CURRENT_TIMESTAMP")
    long countOverdueLoans();

    // ========== JOIN 예제 메소드들 ==========

    /**
     * 회원 이름으로 대여 조회 (JOIN 사용)
     */
    @Query("SELECT l FROM Loan l JOIN l.member m WHERE m.name = :name")
    List<Loan> findByMemberName(@Param("name") String name);

    /**
     * 도서 제목으로 대여 조회 (JOIN 사용)
     */
    @Query("SELECT l FROM Loan l JOIN l.book b WHERE b.title = :title")
    List<Loan> findByBookTitle(@Param("title") String title);

    /**
     * 특정 회원의 연체된 대여 조회 (JOIN 사용)
     */
    @Query("SELECT l FROM Loan l JOIN l.member m " +
           "WHERE m.email = :email AND l.status = com.example.spring.entity.LoanStatus.OVERDUE")
    List<Loan> findOverdueLoansByMemberEmail(@Param("email") String email);

    /**
     * N+1 문제 해결: Fetch Join으로 회원, 도서 정보 함께 조회
     */
    @Query("SELECT l FROM Loan l " +
           "JOIN FETCH l.member " +
           "JOIN FETCH l.book")
    List<Loan> findAllWithMemberAndBook();

    /**
     * 특정 도서를 대여한 회원 목록 조회
     */
    @Query("SELECT DISTINCT l.member FROM Loan l " +
           "JOIN l.book b " +
           "WHERE b.title = :bookTitle")
    List<Member> findMembersByBookTitle(@Param("bookTitle") String bookTitle);

    /**
     * 특정 회원의 현재 대여 중인 도서 목록
     */
    @Query("SELECT l.book FROM Loan l " +
           "WHERE l.member.id = :memberId " +
           "AND l.returnDate IS NULL")
    List<Book> findCurrentlyBorrowedBooks(@Param("memberId") Long memberId);

    /**
     * 연체 중인 대여 조회 (회원 정보 포함)
     */
    @Query("SELECT l FROM Loan l " +
           "JOIN FETCH l.member m " +
           "WHERE l.status = com.example.spring.entity.LoanStatus.OVERDUE " +
           "ORDER BY l.dueDate ASC")
    List<Loan> findOverdueLoansWithMember();

    // ========== Default 메소드 ==========

    default List<Loan> findOverdueLoans() {
        return findOverdueLoans(LocalDateTime.now());
    }

    default Loan findLoanById(Long id) {
        return findById(id).orElse(null);
    }
}
