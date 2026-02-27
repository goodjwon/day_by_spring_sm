package com.example.spring.domain.service;

import com.example.spring.domain.model.Book;
import com.example.spring.domain.model.Loan;
import com.example.spring.domain.model.Member;
import com.example.spring.exception.LoanException;
import com.example.spring.domain.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 대출 자격 검증 도메인 서비스
 *
 * 회원이 특정 도서를 대출할 수 있는지 검증합니다.
 * 대출 정책에 대한 비즈니스 규칙을 중앙화합니다.
 */
@Component
@RequiredArgsConstructor
public class LoanEligibilityChecker {

    private final LoanRepository loanRepository;

    /**
     * 회원 1인당 최대 대출 가능 도서 수
     */
    private static final int MAX_LOAN_COUNT = 5;

    /**
     * 대출 가능 여부 확인 (예외 없이 boolean 반환)
     *
     * @param member 회원
     * @param book 도서
     * @return 대출 가능 여부
     */
    public boolean canLoan(Member member, Book book) {
        try {
            validateLoanRequest(member, book);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * 대출 요청 검증 (실패 시 예외 발생)
     *
     * @param member 회원
     * @param book 도서
     * @throws LoanException 대출 불가 시
     */
    public void validateLoanRequest(Member member, Book book) {
        validateBookAvailability(book);
        validateMemberEligibility(member);
    }

    /**
     * 도서 대출 가능 여부 검증
     *
     * @param book 도서
     * @throws LoanException.BookNotAvailableException 도서가 대출 불가 상태일 때
     * @throws LoanException.BookAlreadyLoanedException 도서가 이미 대출 중일 때
     */
    public void validateBookAvailability(Book book) {
        // 1. 도서 재고 확인
        if (!book.getAvailable()) {
            throw new LoanException.BookNotAvailableException(book.getId());
        }

        // 2. 도서가 이미 대여 중인지 확인
        if (loanRepository.existsByBookIdAndReturnDateIsNull(book.getId())) {
            throw new LoanException.BookAlreadyLoanedException(book.getId());
        }
    }

    /**
     * 회원 대출 자격 검증
     *
     * @param member 회원
     * @throws LoanException.LoanLimitExceededException 대출 한도 초과 시
     * @throws LoanException.OverdueLoansExistException 연체 도서가 있을 때
     */
    public void validateMemberEligibility(Member member) {
        // 1. 회원의 현재 대여 도서 수 확인
        List<Loan> activeLoans = loanRepository.findByMemberIdAndReturnDateIsNull(member.getId());
        int currentLoans = activeLoans.size();

        if (currentLoans >= MAX_LOAN_COUNT) {
            throw new LoanException.LoanLimitExceededException(member.getId(), currentLoans, MAX_LOAN_COUNT);
        }

        // 2. 회원의 연체 여부 확인
        boolean hasOverdueLoans = activeLoans.stream()
                .anyMatch(Loan::isOverdue);

        if (hasOverdueLoans) {
            throw new LoanException.OverdueLoansExistException(member.getId());
        }
    }

    /**
     * 도서 대출 가능 여부 확인 (예외 없이 boolean 반환)
     *
     * @param book 도서
     * @return 도서 대출 가능 여부
     */
    public boolean isBookAvailable(Book book) {
        if (!book.getAvailable()) {
            return false;
        }
        return !loanRepository.existsByBookIdAndReturnDateIsNull(book.getId());
    }

    /**
     * 회원 대출 가능 여부 확인 (예외 없이 boolean 반환)
     *
     * @param member 회원
     * @return 회원 대출 가능 여부
     */
    public boolean isMemberEligible(Member member) {
        List<Loan> activeLoans = loanRepository.findByMemberIdAndReturnDateIsNull(member.getId());

        // 대출 한도 확인
        if (activeLoans.size() >= MAX_LOAN_COUNT) {
            return false;
        }

        // 연체 여부 확인
        return activeLoans.stream().noneMatch(Loan::isOverdue);
    }

    /**
     * 회원의 현재 대출 수 조회
     *
     * @param member 회원
     * @return 현재 대출 수
     */
    public int getCurrentLoanCount(Member member) {
        return loanRepository.findByMemberIdAndReturnDateIsNull(member.getId()).size();
    }

    /**
     * 회원의 남은 대출 가능 수 조회
     *
     * @param member 회원
     * @return 남은 대출 가능 수
     */
    public int getRemainingLoanCount(Member member) {
        int currentLoans = getCurrentLoanCount(member);
        return Math.max(0, MAX_LOAN_COUNT - currentLoans);
    }

    /**
     * 최대 대출 가능 수 조회
     */
    public int getMaxLoanCount() {
        return MAX_LOAN_COUNT;
    }
}