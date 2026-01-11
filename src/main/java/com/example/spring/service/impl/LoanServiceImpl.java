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
import com.example.spring.exception.MemberNotFoundException;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    private static final BigDecimal DAIL_OVERDUE_FEE = new BigDecimal("500");
    private static final int MAX_LOAN_COUNT = 5;

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
    @Override
    public Page<LoanResponse> getAllLoansWithPagination(Pageable pageable, String searchQuery, String statusFilter) {
        log.info("대여 목록 페이징 조회 - 페이지: {}, 검색어: {}, 상태: {}", pageable.getPageNumber(),
                searchQuery, statusFilter);

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
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));

        updateLoanStatus(loan, request.getStatus());

        extendDueDate(loan, request.getDueDate());

        Loan savedLoan = loanRepository.save(loan);

        log.info("대여 정보 수정 완료 - ID: {}, 상태: {}, 반납예정일: {}", savedLoan.getId(), savedLoan.getStatus(), savedLoan.getDueDate());
        return LoanResponse.form(savedLoan);
    }

    private void updateLoanStatus(Loan loan, LoanStatus newStatus) {
        if (newStatus == null) {
            return;
        }
        if (newStatus == LoanStatus.RETURNED) {
            processReturn(loan);
            return;
        }
        loan.setStatus(newStatus);
    }

    private void processReturn(Loan loan) {
        loan.returnBook();

        Book book = loan.getBook();
        book.setAvailable(true);
        bookRepository.save(book);
    }

    private void extendDueDate(Loan loan, LocalDateTime newDueDate) {
        if (newDueDate == null) {
            return;
        }

        if (newDueDate.isBefore(LocalDateTime.now()) || newDueDate.isEqual(LocalDateTime.now())) {
            throw new IllegalArgumentException("반납 예정일은 현재 시간보다 미래여야 합니다");
        }

        loan.setDueDate(newDueDate);
        loan.setStatus(LoanStatus.ACTIVE);
        log.info("대여 기간 연장 완료 - 대여 ID: {}, 새 반납예정일: {}", loan.getId(), newDueDate);
    }

    @Override
    public void deleteLoan(Long loanId) {
        log.info("대여 삭제 요청 - ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("대여 정보를 찾을 수 없습니다"));

        if (loan.getReturnDate() == null) {
            Book book = loan.getBook();
            book.setAvailable(true);
            bookRepository.save(book);
        }
        loanRepository.delete(loan);
        log.info("대여 삭제 완료 - ID: {}", loanId);
    }

    @Override
    public List<LoanResponse> getMyLoans(Long memberId, String statusFilter) {
        log.debug("내 대출 목록 조회 - 회원 ID: {}, 상태 필터: {}", memberId, statusFilter);

        Optional<Member> member = memberRepository.findById(memberId);

        if (member.isEmpty()) {
            throw new EntityNotFoundException("회원 정보를 조회할 수 없습니다");
        }

        List<Loan> loanList = loanRepository.findAll(
                LoanSpecification.byMemberId(memberId));

        return loanList.stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }

    @Override
    public LoanResponse returnBookByMember(Long loanId, Long memberId) {
        log.info("회원 직접 반납 요청 - 대여 ID: {}, 회원 ID: {}", loanId, memberId);

        if (memberId == null) {
            throw new EntityNotFoundException("회원 정보를 조회할 수 없습니다");
        }
        Loan loan = loanRepository.findLoanById(loanId);

        // 소유권 확인
        if (!loan.getMember().getId().equals(memberId)) {
            throw new LoanException.UnauthorizedAccessException(
                    "해당 대출 기록에 접근할 권한이 없습니다. 대출 ID: " + loanId
            );
        }

        if (loan.getReturnDate() != null) {
            throw new LoanException.AlreadyReturnedException(loanId);
        }

        loan.returnBook();
        Loan savedLoan = loanRepository.save(loan);

        Book book = loan.getBook();
        book.setAvailable(true);

        log.info("회원 직접 반납 완료 - 대여 ID: {}", loanId);
        return LoanResponse.form(savedLoan);
    }

    @Override
    public List<LoanResponse> getAllLoans() {
        log.info("전체 대여 목록 조회 요청");

        return loanRepository.findAll().stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getLoansByMemberId(Long memberId) {
        log.info("회원별 대여 목록 조회 - 회원 ID: {}", memberId);

        List<Loan> loanList = loanRepository.findByMemberId(memberId);

        if (memberId == null) {
            throw new EntityNotFoundException("회원 정보를 조회할 수 없습니다");
        }

        return loanList.stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getLoansByBookId(Long bookId) {
        log.info("도서별 대여 목록 조회 - 도서 ID: {}", bookId);

        List<Loan> loanList = loanRepository.findByBookId(bookId);

        if (bookId == null) {
            throw new IllegalArgumentException("도서 정보를 조회할 수 없습니다");
        }

        return loanList.stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getActiveLoans() {
        log.info("활성화 되어있는 모든 대여 목록 조회");

        return loanRepository.findByReturnDateIsNull().stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getActiveLoansByMemberId(Long memberId) {
        log.info("회원별 대여 중인 대여 조회 - 회원 ID: {}", memberId);

        List<Loan> loanList = loanRepository.findByMemberIdAndReturnDateIsNull(memberId);

        if (memberId == null) {
            throw new EntityNotFoundException("회원 정보를 조회할 수 없습니다");
        }

        return loanList.stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getOverdueLoans() {
        log.info("연체된 대여 목록 조회");

        LocalDateTime now = LocalDateTime.now();

        return loanRepository.findOverdueLoans(now).stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getLoansByDateRange(LocalDateTime startDate,
                                                  LocalDateTime endDate) {
        log.info("기간별 대여 조회 - 시작: {}, 종료: {}", startDate, endDate);

        return loanRepository.findByLoanDateBetween(startDate, endDate).stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }

    @Override
    public LoanResponse returnBook(Long loanId) {
        log.info("관리자 도서 반납 요청 - 대여 ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));
        if (loan.getStatus().equals(LoanStatus.RETURNED)) {
            throw new LoanException.AlreadyReturnedException(loanId);
        }
        loan.returnBook();
        Book book = loan.getBook();
        book.setAvailable(true);

        log.info("도서 반납 완료 - 대여 ID: {}, 도서명: {}", loanId, book.getTitle());
        return LoanResponse.form(loan);
    }

    @Override
    public LoanResponse extendLoan(Long loanId, ExtendLoanRequest request) {
        log.info("대여 연장 요청 - 대여 ID: {}, 연장일수: {}", loanId, request.getDays());

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));

        if (loan.getReturnDate() != null) {
            throw new LoanException.AlreadyReturnedException(loanId);
        }
        if (loan.getDueDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("연체된 도서는 연장할 수 없습니다");
        }

        loan.extendLoan(request.getDays());
        log.info("대여 연장 완료 - 새 반납예정일: {}", loan.getDueDate());

        return LoanResponse.form(loan);
    }

    @Override
    @Transactional
    public void cancelLoan(Long loanId) {
        log.info("대여 취소 요청 - ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));

        if (loan.getReturnDate() != null) {
            throw new LoanException.AlreadyReturnedException(loanId);
        }
        Book book = loan.getBook();
        book.setAvailable(true);

        loan.cancel();
        log.info("대여 취소 완료 - ID: {}", loanId);
    }

    @Override
    public BigDecimal getOverdueFee(Long loanId) {
        log.debug("연체료 계산 요청 - 대여 ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(loanId));

        if (loan.getDueDate().isAfter(LocalDateTime.now())) {
            return BigDecimal.ZERO;
        }

        long overdueDays = loan.getOverdueDays();
        if (overdueDays <= 0) return BigDecimal.ZERO;

        return DAIL_OVERDUE_FEE.multiply(BigDecimal.valueOf(overdueDays));
    }

    @Override
    public long getOverdueLoansCount() {
        log.debug("연체 건수 조회");

        return loanRepository.countOverdueLoans();
    }

    @Override
    public boolean canMemberLoan(Long memberId) {
        log.debug("대여 가능 여부 확인 - 회원 ID: {}", memberId);

        if (memberRepository.findMemberById(memberId) == null) {
            throw new MemberNotFoundException(memberId);
        }

        List<Loan> loanList = loanRepository.findByMemberIdAndReturnDateIsNull(memberId);

        return loanList.size() < MAX_LOAN_COUNT;
    }

    @Override
    public boolean isBookAvailableForLoan(Long bookId) {
        log.debug("도서 대여 가능 확인 - 도서 ID: {}", bookId);

        return bookRepository.findById(bookId)
                .map(Book::getAvailable)
                .orElseThrow(() -> new EntityNotFoundException("도서를 찾을 수 없습니다"));
    }

    @Override
    public List<LoanResponse> getLoansByMemberName(String name) {
        log.info("회원명으로 대여 검색 - 이름: {}", name);

        return loanRepository.findByMemberName(name).stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getLoansByBookTitle(String title) {
        log.info("도서명으로 대여 검색 - 제목: {}", title);

        return loanRepository.findByBookTitle(title).stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getOverdueLoansByMemberEmail(String email) {
        log.info("이메일로 연체 목록 조회 - 이메일: {}", email);

        return loanRepository.findOverdueLoansByMemberEmail(email, LoanStatus.OVERDUE).stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getAllLoansWithDetails() {
        log.info("대여 상세 목록(Fetch Join) 조회");

        return loanRepository.findAllWithMemberAndBook().stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberResponse> getMembersByBookTitle(String bookTitle) {
        log.info("특정 도서 대여 회원 조회 - 도서명: {}", bookTitle);

        return loanRepository.findMembersByBookTitle(bookTitle).stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> getCurrentlyBorrowedBooksByMember(Long memberId) {
        log.info("회원이 현재 대여 중인 도서 조회 - 회원 ID: {}", memberId);

        return loanRepository.findCurrentlyBorrowedBooks(memberId).stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getOverdueLoansWithMemberInfo() {
        log.info("연체 목록 상세 조회");

        return loanRepository.findOverdueLoansWithMember(LoanStatus.OVERDUE).stream()
                .map(LoanResponse::form)
                .collect(Collectors.toList());
    }
}
