package com.example.spring.service;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import com.example.spring.dto.request.CreateLoanRequest;
import com.example.spring.dto.request.ExtendLoanRequest;
import com.example.spring.dto.response.LoanResponse;
import com.example.spring.entity.Book;
import com.example.spring.entity.Loan;
import com.example.spring.entity.LoanStatus;
import com.example.spring.entity.Member;
import com.example.spring.entity.MembershipType;
import com.example.spring.entity.Role;
import com.example.spring.exception.BookException;

import com.example.spring.exception.LoanException;
import com.example.spring.exception.MemberException;
import com.example.spring.repository.BookRepository;
import com.example.spring.repository.LoanRepository;
import com.example.spring.repository.MemberRepository;
import com.example.spring.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService 테스트")
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Member testMember;
    private Book testBook;
    private Loan testLoan;
    private CreateLoanRequest createLoanRequest;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .name("홍길동")
                .email("hong@example.com")
                .password("test-password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();

        testBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn(ISBN.of("9780132350884"))
                .price(Money.of(new BigDecimal("45000")))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();

        testLoan = Loan.builder()
                .id(1L)
                .member(testMember)
                .book(testBook)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .status(LoanStatus.ACTIVE)
                .createdDate(LocalDateTime.now())
                .build();

        createLoanRequest = CreateLoanRequest.builder()
                .memberId(1L)
                .bookId(1L)
                .loanDays(14)
                .build();
    }

    @Nested
    @DisplayName("대여 생성")
    class CreateLoanTest {

        @Test
        @DisplayName("정상적인 대여 생성")
        void createLoan_유효한요청_생성성공() {
            // Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(1L)).willReturn(false);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).willReturn(List.of());
            given(loanRepository.save(any(Loan.class))).willReturn(testLoan);
            given(bookRepository.save(any(Book.class))).willReturn(testBook);

            // When
            LoanResponse result = loanService.createLoan(createLoanRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getMemberId()).isEqualTo(1L);
            assertThat(result.getBookId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(LoanStatus.ACTIVE);

            verify(loanRepository).save(any(Loan.class));
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("존재하지 않는 회원 - 예외 발생")
        void createLoan_존재하지않는회원_예외발생() {
            // Given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(MemberException.MemberNotFoundException.class)
                    .hasMessageContaining("회원을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 도서 - 예외 발생")
        void createLoan_존재하지않는도서_예외발생() {
            // Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(BookException.BookNotFoundException.class)
                    .hasMessageContaining("도서를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("도서가 이미 대여 중 - 예외 발생")
        void createLoan_도서이미대여중_예외발생() {
            // Given
            testBook.setAvailability(true);
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(1L)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(LoanException.BookAlreadyLoanedException.class)
                    .hasMessageContaining("도서가 이미 대여 중입니다");
        }

        @Test
        @DisplayName("회원 대여 한도 초과 - 예외 발생")
        void createLoan_대여한도초과_예외발생() {
            // Given
            List<Loan> existingLoans = List.of(
                    Loan.builder().build(),
                    Loan.builder().build(),
                    Loan.builder().build(),
                    Loan.builder().build(),
                    Loan.builder().build()
            );

            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(1L)).willReturn(false);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).willReturn(existingLoans);

            // When & Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(LoanException.LoanLimitExceededException.class)
                    .hasMessageContaining("대여 한도를 초과했습니다");
        }

        @Test
        @DisplayName("연체 중인 대여가 있는 경우 - 예외 발생")
        void createLoan_연체중인대여존재_예외발생() {
            // Given
            Loan overdueLoan = Loan.builder()
                    .id(2L)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusWeeks(3))
                    .dueDate(LocalDateTime.now().minusWeeks(1))
                    .status(LoanStatus.OVERDUE)
                    .createdDate(LocalDateTime.now())
                    .build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(1L)).willReturn(false);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).willReturn(List.of(overdueLoan));

            // When & Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(LoanException.OverdueLoansExistException.class)
                    .hasMessageContaining("연체 중인 도서가 있습니다");
        }

        @Test
        @DisplayName("도서가 대여 불가능 상태(available=false) - 예외 발생")
        void createLoan_도서대여불가능_예외발생() {
            // Given
            testBook.loanOut(); // 대여 불가능 상태로 변경
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));

            // When & Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(LoanException.BookNotAvailableException.class)
                    .hasMessageContaining("도서가 대여 불가능합니다");
        }

        @Test
        @DisplayName("대여 생성 시 도서 상태가 available=false로 변경됨")
        void createLoan_생성시_도서상태변경확인() {
            // Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(1L)).willReturn(false);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).willReturn(List.of());
            given(loanRepository.save(any(Loan.class))).willReturn(testLoan);
            given(bookRepository.save(any(Book.class))).willAnswer(invocation -> {
                Book savedBook = invocation.getArgument(0);
                assertThat(savedBook.getAvailable()).isFalse(); // 도서 상태가 false로 변경되었는지 확인
                return savedBook;
            });

            // When
            loanService.createLoan(createLoanRequest);

            // Then
            verify(bookRepository).save(argThat(book -> !book.getAvailable()));
        }

        @Test
        @DisplayName("대여 기간 7일로 생성")
        void createLoan_대여기간7일_생성성공() {
            // Given
            CreateLoanRequest request = CreateLoanRequest.builder()
                    .memberId(1L)
                    .bookId(1L)
                    .loanDays(7)
                    .build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(1L)).willReturn(false);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).willReturn(List.of());
            given(loanRepository.save(any(Loan.class))).willReturn(testLoan);
            given(bookRepository.save(any(Book.class))).willReturn(testBook);

            // When
            LoanResponse result = loanService.createLoan(request);

            // Then
            assertThat(result).isNotNull();
            verify(loanRepository).save(any(Loan.class));
        }

        @Test
        @DisplayName("대여 기간 30일로 생성")
        void createLoan_대여기간30일_생성성공() {
            // Given
            CreateLoanRequest request = CreateLoanRequest.builder()
                    .memberId(1L)
                    .bookId(1L)
                    .loanDays(30)
                    .build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(1L)).willReturn(false);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).willReturn(List.of());
            given(loanRepository.save(any(Loan.class))).willReturn(testLoan);
            given(bookRepository.save(any(Book.class))).willReturn(testBook);

            // When
            LoanResponse result = loanService.createLoan(request);

            // Then
            assertThat(result).isNotNull();
            verify(loanRepository).save(any(Loan.class));
        }

        @Test
        @DisplayName("대여 생성 시 loanDate와 dueDate가 올바르게 설정됨")
        void createLoan_생성시_날짜설정확인() {
            // Given
            CreateLoanRequest request = CreateLoanRequest.builder()
                    .memberId(1L)
                    .bookId(1L)
                    .loanDays(14)
                    .build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(1L)).willReturn(false);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).willReturn(List.of());
            given(loanRepository.save(any(Loan.class))).willAnswer(invocation -> {
                Loan savedLoan = invocation.getArgument(0);
                assertThat(savedLoan.getLoanDate()).isNotNull();
                assertThat(savedLoan.getDueDate()).isNotNull();
                assertThat(savedLoan.getDueDate()).isAfter(savedLoan.getLoanDate());
                return testLoan;
            });
            given(bookRepository.save(any(Book.class))).willReturn(testBook);

            // When
            loanService.createLoan(request);

            // Then
            verify(loanRepository).save(argThat(loan ->
                    loan.getLoanDate() != null &&
                            loan.getDueDate() != null &&
                            loan.getDueDate().isAfter(loan.getLoanDate())
            ));
        }

        @Test
        @DisplayName("loanDays가 null인 경우 기본값 14일 적용")
        void createLoan_대여기간null_기본값14일적용() {
            // Given
            CreateLoanRequest request = CreateLoanRequest.builder()
                    .memberId(1L)
                    .bookId(1L)
                    .loanDays(null)
                    .build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(1L)).willReturn(false);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).willReturn(List.of());
            given(loanRepository.save(any(Loan.class))).willReturn(testLoan);
            given(bookRepository.save(any(Book.class))).willReturn(testBook);

            // When
            LoanResponse result = loanService.createLoan(request);

            // Then
            assertThat(result).isNotNull();
            verify(loanRepository).save(any(Loan.class));
        }

        @Test
        @DisplayName("회원 ID가 null인 경우 - 예외 발생")
        void createLoan_회원IDnull_예외발생() {
            // Given
            CreateLoanRequest request = CreateLoanRequest.builder()
                    .memberId(null)
                    .bookId(1L)
                    .loanDays(14)
                    .build();

            given(memberRepository.findById(null)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> loanService.createLoan(request))
                    .isInstanceOf(MemberException.MemberNotFoundException.class)
                    .hasMessageContaining("회원을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("도서 ID가 null인 경우 - 예외 발생")
        void createLoan_도서IDnull_예외발생() {
            // Given
            CreateLoanRequest request = CreateLoanRequest.builder()
                    .memberId(1L)
                    .bookId(null)
                    .loanDays(14)
                    .build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(null)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> loanService.createLoan(request))
                    .isInstanceOf(BookException.BookNotFoundException.class)
                    .hasMessageContaining("도서를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("대여 생성 시 LoanResponse의 모든 필드가 올바르게 매핑됨")
        void createLoan_응답DTO_필드매핑확인() {
            // Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(1L)).willReturn(false);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).willReturn(List.of());
            given(loanRepository.save(any(Loan.class))).willReturn(testLoan);
            given(bookRepository.save(any(Book.class))).willReturn(testBook);

            // When
            LoanResponse result = loanService.createLoan(createLoanRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testLoan.getId());
            assertThat(result.getMemberId()).isEqualTo(testMember.getId());
            assertThat(result.getMemberName()).isEqualTo(testMember.getName());
            assertThat(result.getMemberEmail()).isEqualTo(testMember.getEmail());
            assertThat(result.getBookId()).isEqualTo(testBook.getId());
            assertThat(result.getBookTitle()).isEqualTo(testBook.getTitle());
            assertThat(result.getBookAuthor()).isEqualTo(testBook.getAuthor());
            assertThat(result.getBookIsbn()).isEqualTo(testBook.getIsbn().getValue());
            assertThat(result.getStatus()).isEqualTo(LoanStatus.ACTIVE);
            assertThat(result.getLoanDate()).isNotNull();
            assertThat(result.getDueDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("대여 조회")
    class GetLoanTest {

        @Test
        @DisplayName("ID로 대여 조회 성공")
        void getLoanById_존재하는대여_조회성공() {
            // Given
            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));

            // When
            Optional<LoanResponse> result = loanService.getLoanById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getMemberName()).isEqualTo("홍길동");
            assertThat(result.get().getBookTitle()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("회원별 대여 내역 조회")
        void getLoansByMemberId_회원ID로조회_목록반환() {
            // Given
            given(loanRepository.findByMemberId(1L)).willReturn(List.of(testLoan));

            // When
            List<LoanResponse> result = loanService.getLoansByMemberId(1L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMemberId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("현재 대여 중인 목록 조회")
        void getActiveLoans_대여중인목록조회_목록반환() {
            // Given
            given(loanRepository.findByReturnDateIsNull()).willReturn(List.of(testLoan));

            // When
            List<LoanResponse> result = loanService.getActiveLoans();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getReturnDate()).isNull();
        }
    }

    @Nested
    @DisplayName("도서 반납")
    class ReturnBookTest {

        @Test
        @DisplayName("정상적인 도서 반납")
        void returnBook_정상반납_성공() {
            // Given
            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));
            given(loanRepository.save(any(Loan.class))).willReturn(testLoan);
            given(bookRepository.save(any(Book.class))).willReturn(testBook);

            // When
            LoanResponse result = loanService.returnBook(1L);

            // Then
            assertThat(result).isNotNull();
            verify(loanRepository).save(any(Loan.class));
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("존재하지 않는 대여 - 예외 발생")
        void returnBook_존재하지않는대여_예외발생() {
            // Given
            given(loanRepository.findById(1L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> loanService.returnBook(1L))
                    .isInstanceOf(LoanException.LoanNotFoundException.class);
        }

        @Test
        @DisplayName("이미 반납된 대여 - 예외 발생")
        void returnBook_이미반납됨_예외발생() {
            // Given
            ReflectionTestUtils.setField(testLoan, "returnDate", LocalDateTime.now());
            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));

            // When & Then
            assertThatThrownBy(() -> loanService.returnBook(1L))
                    .isInstanceOf(LoanException.AlreadyReturnedException.class);
        }
    }

    @Nested
    @DisplayName("대여 연장")
    class ExtendLoanTest {

        @Test
        @DisplayName("정상적인 대여 연장 - 반납 예정일 3일 이내")
        void extendLoan_정상연장_성공() {
            // Given
            // 연장은 반납 예정일 3일 전부터만 가능하므로, dueDate를 2일 후로 설정
            Loan loanNearDueDate = Loan.builder()
                    .id(1L)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(12))
                    .dueDate(LocalDateTime.now().plusDays(2))  // 반납 예정일 2일 남음 (3일 이내)
                    .status(LoanStatus.ACTIVE)
                    .extensionCount(0)
                    .createdDate(LocalDateTime.now())
                    .build();

            ExtendLoanRequest request = ExtendLoanRequest.builder()
                    .days(7)
                    .build();

            given(loanRepository.findById(1L)).willReturn(Optional.of(loanNearDueDate));
            given(loanRepository.save(any(Loan.class))).willReturn(loanNearDueDate);

            // When
            LoanResponse result = loanService.extendLoan(1L, request);

            // Then
            assertThat(result).isNotNull();
            verify(loanRepository).save(any(Loan.class));
        }

        @Test
        @DisplayName("반납 예정일 3일 전 이전에 연장 시도 - 예외 발생")
        void extendLoan_연장시간제한_예외발생() {
            // Given
            // testLoan은 dueDate가 14일 후이므로 연장 불가
            ExtendLoanRequest request = ExtendLoanRequest.builder()
                    .days(7)
                    .build();

            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));

            // When & Then
            assertThatThrownBy(() -> loanService.extendLoan(1L, request))
                    .isInstanceOf(LoanException.ExtensionTooEarlyException.class)
                    .hasMessageContaining("반납 예정일 3일 전부터 연장 가능합니다");
        }

        @Test
        @DisplayName("연장 횟수 3회 초과 - 예외 발생")
        void extendLoan_연장횟수초과_예외발생() {
            // Given
            Loan loanMaxExtensions = Loan.builder()
                    .id(1L)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(12))
                    .dueDate(LocalDateTime.now().plusDays(2))  // 연장 가능 시간 조건 충족
                    .status(LoanStatus.ACTIVE)
                    .extensionCount(3)  // 이미 3회 연장함
                    .createdDate(LocalDateTime.now())
                    .build();

            ExtendLoanRequest request = ExtendLoanRequest.builder()
                    .days(7)
                    .build();

            given(loanRepository.findById(1L)).willReturn(Optional.of(loanMaxExtensions));

            // When & Then
            assertThatThrownBy(() -> loanService.extendLoan(1L, request))
                    .isInstanceOf(LoanException.ExtensionLimitExceededException.class)
                    .hasMessageContaining("연장 가능 횟수를 초과했습니다");
        }

        @Test
        @DisplayName("존재하지 않는 대여 - 예외 발생")
        void extendLoan_존재하지않는대여_예외발생() {
            // Given
            ExtendLoanRequest request = ExtendLoanRequest.builder()
                    .days(7)
                    .build();
            given(loanRepository.findById(1L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> loanService.extendLoan(1L, request))
                    .isInstanceOf(LoanException.LoanNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("대여 취소")
    class CancelLoanTest {

        @Test
        @DisplayName("정상적인 대여 취소")
        void cancelLoan_정상취소_성공() {
            // Given
            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));
            given(loanRepository.save(any(Loan.class))).willReturn(testLoan);
            given(bookRepository.save(any(Book.class))).willReturn(testBook);

            // When
            loanService.cancelLoan(1L);

            // Then
            verify(loanRepository).save(any(Loan.class));
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("존재하지 않는 대여 - 예외 발생")
        void cancelLoan_존재하지않는대여_예외발생() {
            // Given
            given(loanRepository.findById(1L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> loanService.cancelLoan(1L))
                    .isInstanceOf(LoanException.LoanNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("대여 가능 여부 확인")
    class CheckAvailabilityTest {

        @Test
        @DisplayName("회원 대여 가능")
        void canMemberLoan_정상회원_true반환() {
            // Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).willReturn(List.of());

            // When
            boolean result = loanService.canMemberLoan(1L);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("회원 대여 불가 - 한도 초과")
        void canMemberLoan_한도초과_false반환() {
            // Given
            List<Loan> existingLoans = List.of(
                    Loan.builder().loanDate(LocalDateTime.now()).dueDate(LocalDateTime.now().plusDays(14)).build(),
                    Loan.builder().loanDate(LocalDateTime.now()).dueDate(LocalDateTime.now().plusDays(14)).build(),
                    Loan.builder().loanDate(LocalDateTime.now()).dueDate(LocalDateTime.now().plusDays(14)).build(),
                    Loan.builder().loanDate(LocalDateTime.now()).dueDate(LocalDateTime.now().plusDays(14)).build(),
                    Loan.builder().loanDate(LocalDateTime.now()).dueDate(LocalDateTime.now().plusDays(14)).build()
            );

            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).willReturn(existingLoans);

            // When
            boolean result = loanService.canMemberLoan(1L);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("도서 대여 가능")
        void isBookAvailableForLoan_대여가능도서_true반환() {
            // Given
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(1L)).willReturn(false);

            // When
            boolean result = loanService.isBookAvailableForLoan(1L);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("도서 대여 불가 - 이미 대여 중")
        void isBookAvailableForLoan_이미대여중_false반환() {
            // Given
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(1L)).willReturn(true);

            // When
            boolean result = loanService.isBookAvailableForLoan(1L);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("페이징된 대여 목록 조회")
    class GetAllLoansWithPaginationTest {

        private Member testMember2;
        private Book testBook2;
        private Loan testLoan2;

        @BeforeEach
        void setUp() {
            testMember2 = Member.builder()
                    .id(2L)
                    .name("김철수")
                    .email("kim@example.com")
                    .password("test-password")
                    .role(Role.USER)
                    .membershipType(MembershipType.PREMIUM)
                    .joinDate(LocalDateTime.now())
                    .build();

            testBook2 = Book.builder()
                    .id(2L)
                    .title("Effective Java")
                    .author("Joshua Bloch")
                    .isbn(ISBN.of("9780134685991"))
                    .price(Money.of(new BigDecimal("38000")))
                    .available(false)
                    .createdDate(LocalDateTime.now())
                    .build();

            testLoan2 = Loan.builder()
                    .id(2L)
                    .member(testMember2)
                    .book(testBook2)
                    .loanDate(LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(14))
                    .status(LoanStatus.ACTIVE)
                    .createdDate(LocalDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("기본 페이징 조회 - 정렬 없이")
        void getAllLoansWithPagination_기본페이징_성공() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Loan> loans = List.of(testLoan, testLoan2);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, loans.size());

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("도서명으로 검색")
        void getAllLoansWithPagination_도서명검색_성공() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Loan> loans = List.of(testLoan);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, 1);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, "Clean Code", null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getBookTitle()).isEqualTo("Clean Code");
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("회원명으로 검색")
        void getAllLoansWithPagination_회원명검색_성공() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Loan> loans = List.of(testLoan);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, 1);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, "홍길동", null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getMemberName()).isEqualTo("홍길동");
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("이메일로 검색")
        void getAllLoansWithPagination_이메일검색_성공() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Loan> loans = List.of(testLoan);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, 1);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, "hong@example.com", null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getMemberEmail()).isEqualTo("hong@example.com");
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("상태 필터 - ACTIVE")
        void getAllLoansWithPagination_상태필터ACTIVE_성공() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Loan> loans = List.of(testLoan, testLoan2);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, 2);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, null, "ACTIVE");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(loan -> loan.getStatus() == LoanStatus.ACTIVE);
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("상태 필터 - OVERDUE")
        void getAllLoansWithPagination_상태필터OVERDUE_성공() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Loan overdueLoan = Loan.builder()
                    .id(3L)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(30))
                    .dueDate(LocalDateTime.now().minusDays(2))
                    .status(LoanStatus.OVERDUE)
                    .createdDate(LocalDateTime.now())
                    .build();
            List<Loan> loans = List.of(overdueLoan);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, 1);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, null, "OVERDUE");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(LoanStatus.OVERDUE);
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("상태 필터 - RETURNED")
        void getAllLoansWithPagination_상태필터RETURNED_성공() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Loan returnedLoan = Loan.builder()
                    .id(4L)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(7))
                    .dueDate(LocalDateTime.now().plusDays(7))
                    .returnDate(LocalDateTime.now())
                    .status(LoanStatus.RETURNED)
                    .createdDate(LocalDateTime.now())
                    .build();
            List<Loan> loans = List.of(returnedLoan);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, 1);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, null, "RETURNED");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(LoanStatus.RETURNED);
            assertThat(result.getContent().get(0).getReturnDate()).isNotNull();
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("검색어와 상태 필터 조합")
        void getAllLoansWithPagination_검색어와상태필터조합_성공() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Loan> loans = List.of(testLoan);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, 1);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, "홍길동", "ACTIVE");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getMemberName()).isEqualTo("홍길동");
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(LoanStatus.ACTIVE);
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("빈 검색 결과")
        void getAllLoansWithPagination_빈결과_성공() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Loan> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(emptyPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, "존재하지않는검색어", null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("잘못된 상태 필터는 무시됨 (ALL)")
        void getAllLoansWithPagination_상태필터ALL_모든결과반환() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Loan> loans = List.of(testLoan, testLoan2);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, 2);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, null, "ALL");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("잘못된 상태 필터는 무시됨 (잘못된 값)")
        void getAllLoansWithPagination_잘못된상태필터_무시됨() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Loan> loans = List.of(testLoan, testLoan2);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, 2);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, null, "INVALID_STATUS");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("페이지 크기 제한 확인")
        void getAllLoansWithPagination_페이지크기_확인() {
            // Given
            Pageable pageable = PageRequest.of(0, 5);
            List<Loan> loans = List.of(testLoan, testLoan2);
            Page<Loan> loanPage = new PageImpl<>(loans.subList(0, 2), pageable, 10);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(10);
            assertThat(result.getTotalPages()).isEqualTo(2);
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("두 번째 페이지 조회")
        void getAllLoansWithPagination_두번째페이지_성공() {
            // Given
            Pageable pageable = PageRequest.of(1, 1);
            List<Loan> loans = List.of(testLoan2);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, 2);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getNumber()).isEqualTo(1); // 두 번째 페이지 (0-indexed)
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("빈 검색어는 무시됨")
        void getAllLoansWithPagination_빈검색어_무시됨() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Loan> loans = List.of(testLoan, testLoan2);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, 2);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, "", null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("공백만 있는 검색어는 무시됨")
        void getAllLoansWithPagination_공백검색어_무시됨() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Loan> loans = List.of(testLoan, testLoan2);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, 2);

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            // When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, "   ", null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(loanRepository).findAll(any(Specification.class), eq(pageable));
        }
    }
}
