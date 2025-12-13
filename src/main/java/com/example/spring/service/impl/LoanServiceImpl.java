package com.example.spring.service.impl;

import com.example.spring.dto.request.CreateLoanRequest;
import com.example.spring.dto.request.ExtendLoanRequest;
import com.example.spring.dto.response.LoanResponse;
import com.example.spring.entity.Book;
import com.example.spring.entity.Loan;
import com.example.spring.entity.LoanStatus;
import com.example.spring.repository.LoanRepository;
import com.example.spring.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public LoanResponse createLoan(CreateLoanRequest request) {
        log.info("대여 생성 요청 - 도서명: {}, 대여자: {}", request.getBookId(), request.getMemberId());
        Loan saveLoan = loanRepository.save(request);
        return LoanResponse.form(saveLoan);
    }

    @Override
    public LoanResponse updateLoan(Long id, ExtendLoanRequest request) {
        return null;
    }

    @Override
    public void deleteLoan(Long id) {
        Loan loan = loanRepository.findLoanById(id);
        loanRepository.deleteById(id);
    }

    @Override
    public void deletedLoanRestore(Long id) {
    }

    @Override
    public LoanResponse getLoanById(Loan id) {
        return null;
    }

    @Override
    public Page<LoanResponse> searchLoanWithFilters(Long bookId, Long memberId, LoanStatus status, LocalDateTime dueDate, Pageable pageable) {
        return null;
    }

    @Override
    public Page<LoanResponse> getOverDueByEmail(String email, Pageable pageable) {
        return null;
    }

    @Override
    public Page<LoanResponse> getLoanByMember(Long memberId, Pageable pageable) {
        List<Loan> memberList = loanRepository.findByMemberId(memberId);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), memberList.size());
        List<Loan> pageContent = start >= memberList.size() ? List.of() : memberList.subList(start, end);
        return new PageImpl<>(pageContent);
    }

    @Override
    public Page<LoanResponse> getBookLoanHistory(Long bookId, Pageable pageable) {
        return null;
    }

    @Override
    public LoanResponse getLoanByTitle(String title) {
        return null;
    }

    @Override
    public List<Loan> searchByDueDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return List.of();
    }
}
