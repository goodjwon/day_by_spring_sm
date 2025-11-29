package com.example.spring.service;

import com.example.spring.dto.request.CreateLoanRequest;
import com.example.spring.dto.request.UpdateLoanRequest;
import com.example.spring.dto.response.LoanResponse;
import com.example.spring.entity.Book;
import com.example.spring.entity.Loan;
import com.example.spring.entity.LoanStatus;
import com.example.spring.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public interface LoanService {
    /**
     * 대여 정보 등록
     */
    LoanResponse createLoan(CreateLoanRequest request);
    /**
     * 대여 정보 수정
     */
    LoanResponse updateLoan(Long id, UpdateLoanRequest request);
    /**
     * 대여 기록 복원
     */
    void restoreLoan(Long id);
    /**
     * 대여 기록 삭제(Soft Delete)
     */
    void deleteLoan(Long id);
    /**
     * ID로 대여 기록 조회
     */
    LoanResponse getLoanById(Long id);
    /**
     * LoanStatus로 대여 기록 조회
     */
    Optional<LoanResponse> getLoanByLoanStatus(LoanStatus loanStatus);
    /**
     * 각 대여 상태별 대여 조회
     */
    Page<LoanResponse> getLoans(Pageable pageable);
    /**
     * 복합 조건으로 대여 검색
     * 도서정보
     * 회원정보
     * 대여 상태
     * 대여 날짜
     * @return 검색된 대여 정보
     */
    Page<LoanResponse> searchLoanWithFilters(Book book, Member member,
                                             LoanStatus status, LocalDateTime dueDate,
                                             Pageable pageable);
    /**
     * 대여 날짜 범위로 대여 검색
     */
    List<Loan> searchByDueDateRange(LocalDateTime startDate, LocalDateTime endDate);
    /**
     */
    /**
     */
    /**
     */
    /**
     */
    /**
     */
}
