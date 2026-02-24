package com.example.spring.application.service;

import com.example.spring.application.dto.request.ClientLoanRequest;
import com.example.spring.application.dto.request.CreateLoanRequest;
import com.example.spring.application.dto.request.ExtendLoanRequest;
import com.example.spring.application.dto.request.UpdateLoanRequest;
import com.example.spring.application.dto.response.LoanResponse;
import com.example.spring.domain.model.Book;
import com.example.spring.domain.model.Loan;
import com.example.spring.domain.model.LoanStatus;
import com.example.spring.domain.model.Member;
import com.example.spring.domain.event.LoanCreatedEvent;
import com.example.spring.domain.event.LoanReturnedEvent;
import com.example.spring.exception.BookException.BookNotFoundException;
import com.example.spring.exception.LoanException;
import com.example.spring.exception.MemberException.MemberNotFoundException;
import com.example.spring.exception.ErrorMessages;
import com.example.spring.domain.repository.BookRepository;
import com.example.spring.domain.repository.LoanRepository;
import com.example.spring.domain.repository.LoanSpecification;
import com.example.spring.domain.repository.MemberRepository;
import com.example.spring.application.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * LoanService 구현체
 * 대여 관리 비즈니스 로직을 구현합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 회원의 최대 대여 가능 도서 수
     */
    private static final int MAX_LOAN_COUNT = 5;

    /**
     * 기본 최대 연장 가능 횟수
     */
    private static final int MAX_EXTENSION_COUNT = 3;

    @Override
    @Transactional
    public LoanResponse createLoan(CreateLoanRequest request) {

        // 회원 조회
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다: " + request.getMemberId()));

        // 도서 조회
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookNotFoundException("도서를 찾을 수 없습니다: " + request.getBookId()));

        // 대여 가능 여부 검증
        validateLoanRequest(member, book);

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

        // 도서 재고 상태 업데이트 (대여 중으로 변경)
        book.loanOut();
        bookRepository.save(book);

        // 대출 생성 이벤트 발행
        eventPublisher.publishEvent(new LoanCreatedEvent(savedLoan));

        return LoanResponse.from(savedLoan);
    }



    @Override
    public List<LoanResponse> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getLoansByMemberId(Long memberId) {
        return loanRepository.findByMemberId(memberId).stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getLoansByBookId(Long bookId) {
        return loanRepository.findByBookId(bookId).stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getActiveLoans() {
        return loanRepository.findByReturnDateIsNull().stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getActiveLoansByMemberId(Long memberId) {
        return loanRepository.findByMemberIdAndReturnDateIsNull(memberId).stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getOverdueLoans() {
        return loanRepository.findOverdueLoans().stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getLoansByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return loanRepository.findByLoanDateBetween(startDate, endDate).stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LoanResponse returnBook(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));

        if (loan.getReturnDate() != null) {
            throw new LoanException.AlreadyReturnedException(loanId);
        }

        // 연체 여부 확인 (이벤트 발행 전에 확인)
        boolean wasOverdue = loan.isOverdue();

        // 반납 처리
        loan.returnBook();
        Loan savedLoan = loanRepository.save(loan);

        // 도서 재고 상태 업데이트 (대여 가능으로 변경)
        Book book = loan.getBook();
        book.returnBook();
        bookRepository.save(book);

        // 도서 반납 이벤트 발행
        eventPublisher.publishEvent(new LoanReturnedEvent(savedLoan, wasOverdue));

        return LoanResponse.from(savedLoan);
    }

    @Override
    @Transactional
    public LoanResponse extendLoan(Long loanId, ExtendLoanRequest request) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));

        // 연장 가능 여부 검증
        validateExtension(loan);

        try {
            loan.extendLoan(request.getDays(), MAX_EXTENSION_COUNT);
            Loan savedLoan = loanRepository.save(loan);

            return LoanResponse.from(savedLoan);
        } catch (IllegalStateException e) {
            throw new LoanException.LoanExtensionNotAllowedException(e.getMessage());
        }
    }

    /**
     * 연장 가능 여부 검증
     */
    private void validateExtension(Loan loan) {
        if (loan.getReturnDate() != null) {
            throw new LoanException.AlreadyReturnedException(loan.getId());
        }
        if (loan.isOverdue()) {
            throw new LoanException.LoanExtensionNotAllowedException("연체된 대여는 연장할 수 없습니다.");
        }
        if (loan.getExtensionCount() >= MAX_EXTENSION_COUNT) {
            throw new LoanException.ExtensionLimitExceededException(loan.getExtensionCount(), MAX_EXTENSION_COUNT);
        }
        if (!loan.canExtendNow()) {
            throw new LoanException.ExtensionTooEarlyException(loan.getDaysUntilDue());
        }
    }

    @Override
    @Transactional
    public void cancelLoan(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));

        try {
            loan.cancel();
            loanRepository.save(loan);

            // 도서 재고 상태 복원
            Book book = loan.getBook();
            book.returnBook();
            bookRepository.save(book);
        } catch (IllegalStateException e) {
            throw new LoanException.InvalidLoanStateException(e.getMessage());
        }
    }

    @Override
    public BigDecimal getOverdueFee(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));

        return loan.calculateOverdueFee().getAmount();
    }

    @Override
    public long getOverdueLoansCount() {
        return loanRepository.countOverdueLoans();
    }

    @Override
    public boolean canMemberLoan(Long memberId) {

        // 회원 존재 여부 확인
        if (!memberRepository.findById(memberId).isPresent()) {
            return false;
        }

        // 현재 대여 중인 도서 수 확인
        int currentLoans = loanRepository.findByMemberIdAndReturnDateIsNull(memberId).size();
        if (currentLoans >= MAX_LOAN_COUNT) {
            return false;
        }

        // 연체 중인 대여 확인
        List<Loan> overdueLoans = loanRepository.findByMemberId(memberId).stream()
                .filter(Loan::isOverdue)
                .collect(Collectors.toList());

        return overdueLoans.isEmpty();
    }

    @Override
    public boolean isBookAvailableForLoan(Long bookId) {

        // 도서 존재 및 활성 상태 확인
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty() || !bookOpt.get().getAvailable()) {
            return false;
        }

        // 현재 대여 중인지 확인
        return !loanRepository.existsByBookIdAndReturnDateIsNull(bookId);
    }

    // ========== JOIN 활용 메소드들 구현 ==========

    @Override
    public List<LoanResponse> getLoansByMemberName(String name) {
        return loanRepository.findByMemberName(name).stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getLoansByBookTitle(String title) {
        return loanRepository.findByBookTitle(title).stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getOverdueLoansByMemberEmail(String email) {
        return loanRepository.findOverdueLoansByMemberEmail(email, LoanStatus.OVERDUE).stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getAllLoansWithDetails() {
        return loanRepository.findAllWithMemberAndBook().stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<com.example.spring.application.dto.response.MemberResponse> getMembersByBookTitle(String bookTitle) {
        return loanRepository.findMembersByBookTitle(bookTitle).stream()
                .map(com.example.spring.application.dto.response.MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<com.example.spring.application.dto.response.BookResponse> getCurrentlyBorrowedBooksByMember(Long memberId) {
        return loanRepository.findCurrentlyBorrowedBooks(memberId).stream()
                .map(com.example.spring.application.dto.response.BookResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getOverdueLoansWithMemberInfo() {
        return loanRepository.findOverdueLoansWithMember(LoanStatus.OVERDUE).stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    // ========== API 명세 기반 메서드 구현 ==========

    @Override
    @Transactional(readOnly = true)
    public Page<LoanResponse> getAllLoansWithPagination(Pageable pageable, String searchQuery, String statusFilter) {

        Page<Loan> loanPage = loanRepository.findAll(
                LoanSpecification.withFilters(searchQuery, statusFilter),
                pageable
        );

        return loanPage.map(LoanResponse::from);
    }

    @Override
    public Optional<LoanResponse> getLoanById(Long id) {
        return loanRepository.findById(id)
                .map(LoanResponse::from);
    }

    @Override
    @Transactional
    public LoanResponse updateLoan(Long loanId, UpdateLoanRequest request) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));

        // 상태 업데이트 (반납 처리 등)
        updateLoanStatus(loan, request.getStatus());

        // 반납일 연장
        extendDueDate(loan, request.getDueDate());

        Loan savedLoan = loanRepository.save(loan);
        return LoanResponse.from(savedLoan);
    }

    /**
     * 대여 상태 업데이트 (반납 처리 포함)
     */
    private void updateLoanStatus(Loan loan, LoanStatus newStatus) {
        if (newStatus == null) {
            return;
        }

        if (newStatus == LoanStatus.RETURNED) {
            processReturn(loan);
            return;
        }

        // 기타 상태 변경
        loan.changeStatus(newStatus);
    }

    /**
     * 반납 처리
     */
    private void processReturn(Loan loan) {
        loan.returnBook();

        // 도서 재고 상태 업데이트
        Book book = loan.getBook();
        book.returnBook();
        bookRepository.save(book);
    }

    /**
     * 반납일 연장 (관리자용 - PATCH API에서 직접 dueDate 변경)
     */
    private void extendDueDate(Loan loan, LocalDateTime newDueDate) {
        if (newDueDate == null) {
            return;
        }

        if (newDueDate.isBefore(LocalDateTime.now()) || newDueDate.isEqual(LocalDateTime.now())) {
            throw new IllegalArgumentException(ErrorMessages.LOAN_DUE_DATE_MUST_BE_FUTURE);
        }

        // 연장 횟수 증가
        loan.adminExtendDueDate(newDueDate);
    }

    @Override
    @Transactional
    public void deleteLoan(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));

        // 대여 중인 경우 도서 상태 복원
        if (loan.getReturnDate() == null) {
            Book book = loan.getBook();
            book.returnBook();
            bookRepository.save(book);
        }

        loanRepository.delete(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getMyLoans(Long memberId, String statusFilter) {

        List<Loan> loans = loanRepository.findAll(
                LoanSpecification.byMemberAndStatus(memberId, statusFilter)
        );

        return loans.stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LoanResponse returnBookByMember(Long loanId, Long memberId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));

        // 소유권 확인
        if (!loan.getMember().getId().equals(memberId)) {
            throw new LoanException.UnauthorizedAccessException(
                    "해당 대출 기록에 접근할 권한이 없습니다. 대출 ID: " + loanId
            );
        }

        if (loan.getReturnDate() != null) {
            throw new LoanException.AlreadyReturnedException(loanId);
        }

        // 연체 여부 확인 (이벤트 발행 전에 확인)
        boolean wasOverdue = loan.isOverdue();

        // 반납 처리
        loan.returnBook();
        Loan savedLoan = loanRepository.save(loan);

        // 도서 재고 상태 업데이트
        Book book = loan.getBook();
        book.returnBook();
        bookRepository.save(book);

        // 도서 반납 이벤트 발행
        eventPublisher.publishEvent(new LoanReturnedEvent(savedLoan, wasOverdue));

        return LoanResponse.from(savedLoan);
    }

    @Override
    @Transactional
    public void deleteLoanByMember(Long loanId, Long memberId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));

        // 소유권 확인
        if (!loan.getMember().getId().equals(memberId)) {
            throw new LoanException.UnauthorizedAccessException(
                    "해당 대출 기록에 접근할 권한이 없습니다. 대출 ID: " + loanId
            );
        }

        // RETURNED 상태 확인
        if (loan.getReturnDate() == null || loan.getStatus() != LoanStatus.RETURNED) {
            throw new LoanException.CannotDeleteActiveLoanException(loanId, loan.getStatus());
        }

        loanRepository.delete(loan);
    }

    @Override
    @Transactional
    public LoanResponse createLoanByMember(Long memberId, ClientLoanRequest request) {

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다: " + memberId));

        // 도서 조회
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookNotFoundException("도서를 찾을 수 없습니다: " + request.getBookId()));

        // 대여 가능 여부 검증
        validateLoanRequest(member, book);

        // 대여 생성
        LocalDateTime now = LocalDateTime.now();
        int loanPeriod = request.getLoanPeriod() != null ? request.getLoanPeriod() : 14;
        LocalDateTime dueDate = now.plusDays(loanPeriod);

        Loan loan = Loan.builder()
                .member(member)
                .book(book)
                .loanDate(now)
                .dueDate(dueDate)
                .build();

        Loan savedLoan = loanRepository.save(loan);

        // 도서 재고 상태 업데이트 (대여 중으로 변경)
        book.loanOut();
        bookRepository.save(book);

        // 대출 생성 이벤트 발행
        eventPublisher.publishEvent(new LoanCreatedEvent(savedLoan));

        return LoanResponse.from(savedLoan);
    }

    /**
     * 대여 요청 검증
     */
    private void validateLoanRequest(Member member, Book book) {
        // 1. 도서 재고 확인
        if (!book.getAvailable()) {
            throw new LoanException.BookNotAvailableException(book.getId());
        }

        // 2. 도서가 이미 대여 중인지 확인
        if (loanRepository.existsByBookIdAndReturnDateIsNull(book.getId())) {
            throw new LoanException.BookAlreadyLoanedException(book.getId());
        }

        // 3. 회원의 현재 대여 도서 수 확인
        int currentLoans = loanRepository.findByMemberIdAndReturnDateIsNull(member.getId()).size();
        if (currentLoans >= MAX_LOAN_COUNT) {
            throw new LoanException.LoanLimitExceededException(member.getId(), currentLoans, MAX_LOAN_COUNT);
        }

        // 4. 회원의 연체 여부 확인
        List<Loan> memberLoans = loanRepository.findByMemberIdAndReturnDateIsNull(member.getId());
        boolean hasOverdueLoans = memberLoans.stream()
                .anyMatch(Loan::isOverdue);

        if (hasOverdueLoans) {
            throw new LoanException.OverdueLoansExistException(member.getId());
        }
    }
}