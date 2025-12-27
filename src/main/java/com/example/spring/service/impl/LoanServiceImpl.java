package com.example.spring.service.impl;

import com.example.spring.dto.request.CreateLoanRequest;
import com.example.spring.dto.request.ExtendLoanRequest;
import com.example.spring.dto.request.UpdateLoanRequest;
import com.example.spring.dto.response.BookResponse;
import com.example.spring.dto.response.LoanResponse;
import com.example.spring.dto.response.MemberResponse;
import com.example.spring.entity.Book;
import com.example.spring.entity.Loan;
import com.example.spring.entity.LoanStatus;
import com.example.spring.entity.Member;
import com.example.spring.exception.EntityNotFoundException;
import com.example.spring.exception.LoanException;
import com.example.spring.repository.BookRepository;
import com.example.spring.repository.LoanRepository;
import com.example.spring.repository.LoanSpecification;
import com.example.spring.repository.MemberRepository;
import com.example.spring.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public LoanResponse createLoan(CreateLoanRequest request) {

        log.info("대여 생성 요청 - 회원 ID: {}, 도서 ID: {}", request.getMemberId(), request.getBookId());

        //회원 조회
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다"));
        //도서 조회
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("도서를 찾을 수 없습니다"));


        //도서 제고 확인
        if (!book.getAvailable()) {
            throw new LoanException.BookNotAvailableException(book.getId());
        }

        //도서가 이미 대여 중인지 확인
        if (loanRepository.existsByBookIdAndReturnDateIsNull(book.getId())) {
            throw new LoanException.BookAlreadyLoanedException(book.getId());
        }
        //회원에 현재 도서 대여수 확인

        //연체 여부 확인
        if (loanRepository.existsByMemberAndStatus(member, LoanStatus.OVERDUE)) {
            throw new LoanException.OverdueLoansExistException(request.getMemberId());
        }

        // 대여 생성
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(request.getLoanDays() != null ? request.getLoanDays() : 14);
        Loan loan = Loan.builder()
                .member(member)
                .book(book)
                .loanDate(now)
                .dueDate(dueDate)
                .build();
        Loan savedLoan = loanRepository.save(loan);

        book.setAvailable(false);
        bookRepository.save(book);

        log.info("대여 생성 완료 - 대여 ID: {}, 회원: {}, 도서: {}, 반납예정일: {}",
                savedLoan.getId(), member.getName(), book.getTitle(), dueDate);


        return LoanResponse.form(savedLoan);
    }
    //todo 31 일까지 쿼리 확인
    @Override
    public Page<LoanResponse> getAllLoansWithPagination(Pageable pageable, String searchQuery, String statusFilter) {
        log.info("대여 목록 페이징 조회 - 페이지: {}", pageable.getPageNumber());

        Page<Loan> loanPage = loanRepository.findAll(
                LoanSpecification.withFilters(searchQuery, statusFilter),
                pageable
        );

        return loanPage.map(LoanResponse::form);
    }

    @Override
    public Optional<LoanResponse> getLoanById(Long id) {
        log.info("대여 조회 요청 - ID: {}", id);
        return loanRepository.findById(id)
                .map(LoanResponse::form);
    }

    @Override
    public LoanResponse updateLoan(Long loanId, UpdateLoanRequest request) {
        log.info("대여 정보 수정 요청 - ID: {}", loanId);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("대여 정보를 찾을 수 없습니다"));

        // status 수정 (반납 처리)
        if (request.getStatus() != null) {
            if (request.getStatus() == LoanStatus.RETURNED) {
                loan.returnBook();

                Book book = loan.getBook();
                book.setAvailable(true);
                bookRepository.save(book);
            } else {
                loan.setStatus(request.getStatus());
            }
        }
        // dueDate 수정 (연장)
        if (request.getDueDate() != null) {
            if (request.getDueDate().isAfter(loan.getDueDate())) {
                loan.setDueDate(request.getDueDate());
                loan.setStatus(LoanStatus.ACTIVE);
            } else {
                throw new IllegalArgumentException("반납 예정일은 현재 시간보다 미래여야 합니다");
            }
        }
        loanRepository.save(loan);
        log.info("대여 정보 수정 완료 - ID: {}", loanId);
        return LoanResponse.form(loan);
    }

    @Override
    public void deleteLoan(Long loanId) {
        log.info("대여 삭제 요청 - ID: {}", loanId);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("대여 정보를 찾을 수 없습니다"));

        //todo 대여 중인 경우 도서 상태 복원 (수정)
        if (loan.getReturnDate() == null) {
            Book book = loan.getBook();
            book.setAvailable(true);
            bookRepository.save(book);
        }
        loanRepository.delete(loan);
        log.info("대여 삭제 완료 - ID: {}", loanId);
    }

    //todo 31 일까지 구현하기, 테스트
    @Override
    public List<LoanResponse> getMyLoans(Long memberId, String statusFilter) {
        return List.of();
    }

    //todo 31 일까지 구현하기, 테스트
    @Override
    public LoanResponse returnBookByMember(Long loanId, Long memberId) {
        return null;
    }

    //todo 31 일까지 구현하기, 테스트
    @Override
    public List<LoanResponse> getAllLoans() {
        return List.of();
    }

    //todo 31 일까지 구현하기, 테스트
    @Override
    public List<LoanResponse> getLoansByMemberId(Long memberId) {
        return List.of();
    }

    //todo 31 일까지 구현하기, 테스트
    @Override
    public List<LoanResponse> getLoansByBookId(Long bookId) {
        return List.of();
    }

    //todo 31 일까지 구현하기, 테스트
    @Override
    public List<LoanResponse> getActiveLoans() {
        return List.of();
    }

    //todo 31 일까지 구현하기, 테스트
    @Override
    public List<LoanResponse> getActiveLoansByMemberId(Long memberId) {
        return List.of();
    }

    //todo 31 일까지 구현하기, 테스트
    @Override
    public List<LoanResponse> getOverdueLoans() {
        return List.of();
    }

    //todo 31 일까지 구현하기, 테스트
    @Override
    public List<LoanResponse> getLoansByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return List.of();
    }

    //todo 31 일까지 구현하기, 테스트
    @Override
    public LoanResponse returnBook(Long loanId) {
        return null;
    }

    //todo 31 일까지 구현하기, 테스트
    @Override
    public LoanResponse extendLoan(Long loanId, ExtendLoanRequest request) {
        return null;
    }

    //todo 31 일까지 구현하기, 테스트
    @Override
    public void cancelLoan(Long loanId) {

    }

    @Override
    public BigDecimal getOverdueFee(Long loanId) {
        return null;
    }

    @Override
    public long getOverdueLoansCount() {
        return 0;
    }

    @Override
    public boolean canMemberLoan(Long memberId) {
        return false;
    }

    @Override
    public boolean isBookAvailableForLoan(Long bookId) {
        return false;
    }

    @Override
    public List<LoanResponse> getLoansByMemberName(String name) {
        return List.of();
    }

    @Override
    public List<LoanResponse> getLoansByBookTitle(String title) {
        return List.of();
    }

    @Override
    public List<LoanResponse> getOverdueLoansByMemberEmail(String email) {
        return List.of();
    }

    @Override
    public List<LoanResponse> getAllLoansWithDetails() {
        return List.of();
    }

    @Override
    public List<MemberResponse> getMembersByBookTitle(String bookTitle) {
        return List.of();
    }

    @Override
    public List<BookResponse> getCurrentlyBorrowedBooksByMember(Long memberId) {
        return List.of();
    }

    @Override
    public List<LoanResponse> getOverdueLoansWithMemberInfo() {
        return List.of();
    }
}
