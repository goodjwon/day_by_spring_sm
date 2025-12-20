package com.example.spring.repository;

import com.example.spring.entity.Book;
import com.example.spring.entity.Loan;
import com.example.spring.entity.LoanStatus;
import com.example.spring.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT l FROM Loan l WHERE l.member.id = :memberId ORDER BY l.loanDate DESC")
    List<Loan> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId ORDER BY l.loanDate DESC")
    List<Loan> findByBookId(@Param("bookId") Long bookId);

    @Query("SELECT l FROM Loan l WHERE l.member.id = :memberId AND l.returnDate IS NULL ORDER BY l.loanDate DESC")
    List<Loan> findByMemberIdAndReturnDateIsNull(@Param("memberId") Long memberId);

    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId AND l.returnDate IS NULL")
    List<Loan> findByBookIdAndReturnDateIsNull(@Param("bookId") Long bookId);

    @Query("SELECT l FROM Loan l WHERE l.dueDate < :currentDate AND l.returnDate IS NULL ORDER BY l.dueDate")
    List<Loan> findOverdueLoans(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT l FROM Loan l WHERE l.returnDate IS NULL ORDER BY l.loanDate DESC")
    List<Loan> findByReturnDateIsNull();

    @Query("SELECT l FROM Loan l WHERE l.loanDate BETWEEN :startDate AND :endDate ORDER BY l.loanDate DESC")
    List<Loan> findByLoanDateBetween(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT (COUNT(l) > 0) FROM Loan l WHERE l.book.id = :bookId AND l.returnDate IS NULL")
    boolean existsByBookIdAndReturnDateIsNull(@Param("bookId") Long bookId);

    // 연체 대여 수 조회 (현재 시간 기준)
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.dueDate < CURRENT_TIMESTAMP AND l.returnDate IS NULL")
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
            "WHERE m.email = :email AND l.status = :status")
    List<Loan> findOverdueLoansByMemberEmail(@Param("email") String email, @Param("status") LoanStatus status);

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
            "WHERE l.status = :status " +
            "ORDER BY l.dueDate ASC")
    boolean findOverdueLoansWithMember(@Param("status") LoanStatus status);

    // ========== Default 메소드 ==========

    // 편의 메서드 - 현재 시간 기준 연체 대여 조회
    @Query("SELECT l FROM Loan l")
    Loan findLoanById(Long id);

    @Query("SELECT l.book FROM Loan l " +
            "WHERE l.member.id = :memberId " +
            "AND l.status = :status")
    boolean existsByMemberAndStatus(Member member, LoanStatus loanStatus);
}
