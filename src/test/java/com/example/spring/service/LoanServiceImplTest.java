package com.example.spring.service;

import com.example.spring.dto.request.CreateLoanRequest;
import com.example.spring.dto.request.ExtendLoanRequest;
import com.example.spring.dto.request.UpdateLoanRequest;
import com.example.spring.dto.response.LoanResponse;
import com.example.spring.entity.*;
import com.example.spring.exception.BookException;
import com.example.spring.exception.EntityNotFoundException;
import com.example.spring.exception.LoanException;
import com.example.spring.repository.BookRepository;
import com.example.spring.repository.LoanRepository;
import com.example.spring.repository.MemberRepository;
import com.example.spring.service.impl.BookServiceImpl;
import com.example.spring.service.impl.LoanServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
@Slf4j
public class LoanServiceImplTest {
    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    @InjectMocks
    private BookServiceImpl bookService;

    private CreateLoanRequest createLoanRequest;
    private Member testMember;
    private Book testBook;
    private Loan testLoan;

    @BeforeEach
    void setUp() {
        createLoanRequest = CreateLoanRequest.builder()
                .memberId(1L)
                .bookId(1L)
                .loanDays(14)
                .build();

        testMember = Member.builder()
                .id(1L)
                .name("홍길동")
                .email("hong@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();

        testBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("45000"))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();

        testLoan = Loan.builder()
                .id(1L)
                .member(testMember)
                .book(testBook)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .build();
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
                    .membershipType(MembershipType.PREMIUM)
                    .joinDate(LocalDateTime.now())
                    .build();

            testBook2 = Book.builder()
                    .id(2L)
                    .title("Effective Java")
                    .author("Joshua Bloch")
                    .isbn("9780134685991")
                    .price(new BigDecimal("38000"))
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
            //Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Loan> loans = List.of(testLoan, testLoan2);
            Page<Loan> loanPage = new PageImpl<>(loans, pageable, loans.size());

            given(loanRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(loanPage);

            //When
            Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, null, null);

            //Then
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

    @Nested
    @DisplayName("대여 생성")
    class createLoanTest {

        @Test
        @DisplayName("존재하지 않는 회원 조회")
        void createLoan_존재하지_않는_회원예외발생() {
            //Given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            //When & Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("회원을 찾을 수 없습니다");

        }

        @Test
        @DisplayName("존재하지 않는 도서 조회")
        void createLoan_존재하지_않는_도서예외발생() {
            //Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.empty());

            //When & Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("도서를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("도서 재고 부족")
        void createLoan_도서재고부족() {
            //Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            testBook.setAvailable(false);
            //When
            //Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(LoanException.BookNotAvailableException.class);
        }

        @Test
        @DisplayName("이미 대여 중인 도서 대여 시도")
        void createLoan_대여중인_도서대여시도() {
            //Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(testBook.getId()))
                    .willReturn(true);
            //When
            //Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(LoanException.BookAlreadyLoanedException.class);
        }

        @Test
        @DisplayName("연체 여부 확인")
        void createLoan_연체여부확인() {
            //Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));

            given(loanRepository.existsByMemberAndStatus(testMember, LoanStatus.OVERDUE))
                    .willReturn(true);
            //When
            //Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(LoanException.OverdueLoansExistException.class);
        }

        @Test
        @DisplayName("대여 생성 성공")
        void createLoan_대여생성성공() {
            //Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(loanRepository.existsByBookIdAndReturnDateIsNull(any())).willReturn(false);
            given(loanRepository.existsByMemberAndStatus(any(), any())).willReturn(false);

            given(loanRepository.save(any(Loan.class))).willReturn(testLoan);

            //When
            LoanResponse response = loanService.createLoan(createLoanRequest);

            //Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(testBook.getAvailable()).isFalse();
        }

        @Test
        @DisplayName("대여 조회 - 성공")
        void getLoanById_대여조회_성공() {
            //Given
            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));

            //When
            Optional<LoanResponse> result = loanService.getLoanById(1L);

            //Then
            assertThat(result).isPresent();
            assertThat(result.get().getMemberName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("대여 조회 - 실패")
        void getLoanById_대여조회_실패() {
            //Given
            given(loanRepository.findById(100L)).willReturn(Optional.empty());

            //When
            Optional<LoanResponse> result = loanService.getLoanById(100L);

            //Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("대여 정보 수정 - 상태 변경 성공")
        void updateLoan_상태변경_성공() {
            //Given
            UpdateLoanRequest savedLoan = UpdateLoanRequest.builder()
                    .status(LoanStatus.RETURNED)
                    .build();
            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));
            given(loanRepository.save(any(Loan.class))).willReturn(testLoan);
            //When
            LoanResponse updatedLoan = loanService.updateLoan(testLoan.getId(), savedLoan);
            //Then
            assertThat(updatedLoan).isNotNull();
            assertThat(updatedLoan.getStatus()).isEqualTo(LoanStatus.RETURNED);
            verify(loanRepository).save(any(Loan.class));
        }

        @Test
        @DisplayName("대여 정보 수정 - 대여 연장 성공")
        void updateLoan_대여연장_성공() {
            //Given
            LocalDateTime returnDay = LocalDateTime.now().plusDays(16);
            UpdateLoanRequest savedLoan = UpdateLoanRequest.builder()
                    .dueDate(returnDay)
                    .build();
            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));
            given(loanRepository.save(any(Loan.class))).willReturn(testLoan);
            //When
            LoanResponse updatedLoan = loanService.updateLoan(testLoan.getId(), savedLoan);
            //Then
            assertThat(updatedLoan).isNotNull();
            assertThat(updatedLoan.getDueDate()).isEqualTo(returnDay);
            verify(loanRepository).save(any(Loan.class));
        }

        @Test
        @DisplayName("대여 정보 수정 - 정보 조회 실패")
        void updateLoan_대여정보조회_실패() {
            //Given
            UpdateLoanRequest savedLoan = UpdateLoanRequest.builder().build();
            given(loanRepository.findById(any())).willReturn(Optional.empty());

            //When
            //Then
            assertThatThrownBy(() -> loanService.updateLoan(1L, savedLoan))
                    .isInstanceOf(LoanException.LoanNotFoundException.class);
        }

        @Test
        @DisplayName("대여 삭제(Soft Delete) - 성공")
        void deleteLoan_미반납도서_삭제성공() {
            //Given
            testBook.setAvailable(false);
            Loan ativeLoan = Loan.builder()
                    .id(1L)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now())
                    .returnDate(null)
                    .build();
            given(loanRepository.findById(1L)).willReturn(Optional.of(ativeLoan));

            //When
            loanService.deleteLoan(1L);

            //Then
            verify(loanRepository).delete(ativeLoan);
            assertThat(testBook.getAvailable()).isTrue();
        }

        @Test
        @DisplayName("대여 삭제(Soft Delete) - 성공")
        void deleteLoan_반납된도서_삭제성공() {
            // Given
            testBook.setAvailable(true);
            Loan returnedLoan = Loan.builder()
                    .id(1L)
                    .member(testMember)
                    .book(testBook)
                    .returnDate(LocalDateTime.now())
                    .build();
            given(loanRepository.findById(1L)).willReturn(Optional.of(returnedLoan));

            // When
            loanService.deleteLoan(1L);

            // Then
            verify(loanRepository).delete(returnedLoan);
            assertThat(testBook.getAvailable()).isTrue();
        }

        @Test
        @DisplayName("대여 삭제(Soft Delete) - 대여 조회 실패")
        void deleteLoan_대여삭제_실패() {
            //Given
            given(loanRepository.findById(any())).willReturn(Optional.empty());

            //When
            //Then
            assertThatThrownBy(() -> loanService.deleteLoan(1L))
                    .isInstanceOf(EntityNotFoundException.class);
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

            // When
            LoanResponse result = loanService.returnBook(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(LoanStatus.RETURNED);

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
            testLoan.setReturnDate(LocalDateTime.now().plusDays(7));
            testLoan.setStatus(LoanStatus.RETURNED);
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
        @DisplayName("정상적인 대여 연장")
        void extendLoan_정상연장_성공() {
            // Given
            ExtendLoanRequest request = ExtendLoanRequest.builder()
                    .days(7)
                    .build();

            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));

            // When
            LoanResponse result = loanService.extendLoan(1L, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDueDate()).isEqualTo(testLoan.getDueDate());
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
        @DisplayName("대여 취소 - 성공")
        void cancelLoan_성공() {
            // Given
            testBook.setAvailable(false);
            testLoan.setReturnDate(null);
            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));

            // When
            loanService.cancelLoan(1L);

            // Then
            assertThat(testBook.getAvailable()).isTrue();
            assertThat(testLoan.getStatus()).isEqualTo(LoanStatus.CANCELLED);
        }

        @Test
        @DisplayName("이미 반납된 대여는 취소 불가")
        void cancelLoan_이미반납됨_예외발생() {
            // Given
            testLoan.setReturnDate(LocalDateTime.now());
            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));

            // When & Then
            assertThatThrownBy(() -> loanService.cancelLoan(1L))
                    .isInstanceOf(LoanException.AlreadyReturnedException.class);
        }

        @Test
        @DisplayName("존재하지 않는 대여 취소 시도")
        void cancelLoan_존재하지않음_예외발생() {
            // Given
            given(loanRepository.findById(any())).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> loanService.cancelLoan(1L))
                    .isInstanceOf(LoanException.LoanNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("연체료 계산")
    class OverdueFeeTest {

        @Test
        @DisplayName("연체료 발생 (연체됨)")
        void getOverdueFee_연체료발생() {
            // Given
            testLoan.setDueDate(LocalDateTime.now().minusDays(3));
            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));

            // When
            BigDecimal fee = loanService.getOverdueFee(1L);

            // Then
            assertThat(fee).isEqualByComparingTo(new BigDecimal("1500"));
        }

        @Test
        @DisplayName("연체료 없음 (기한 내)")
        void getOverdueFee_연체료없음() {
            // Given
            testLoan.setDueDate(LocalDateTime.now().plusDays(3));
            given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));

            // When
            BigDecimal fee = loanService.getOverdueFee(1L);

            // Then
            assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("회원 직접 반납")
    class ReturnBookByMemberTest {

        @Test
        @DisplayName("본인의 대여 기록 반납 성공")
        void returnBookByMember_성공() {
            // Given
            given(loanRepository.findLoanById(1L)).willReturn(testLoan);
            given(loanRepository.save(any(Loan.class))).willReturn(testLoan);

            // When
            LoanResponse response = loanService.returnBookByMember(1L, 1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(testLoan.getReturnDate()).isNotNull();
            assertThat(testBook.getAvailable()).isTrue();
        }

        @Test
        @DisplayName("타인의 대여 기록 반납 시도 - 권한 없음")
        void returnBookByMember_권한없음_예외발생() {
            // Given
            given(loanRepository.findLoanById(1L)).willReturn(testLoan);

            // When & Then
            Long otherMemberId = 999L;
            assertThatThrownBy(() -> loanService.returnBookByMember(1L, otherMemberId))
                    .isInstanceOf(LoanException.UnauthorizedAccessException.class);
        }
    }

    @Nested
    @DisplayName("나의 대출 목록 조회")
    class GetMyLoansTest {

        @Test
        @DisplayName("회원의 대출 목록 조회 성공")
        void getMyLoans_성공() {
            // Given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(loanRepository.findAll(any(Specification.class)))
                    .willReturn(List.of(testLoan));

            // When
            List<LoanResponse> results = loanService.getMyLoans(1L, "ACTIVE");

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getMemberName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("존재하지 않는 회원 예외")
        void getMyLoans_회원없음_예외발생() {
            // Given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> loanService.getMyLoans(1L, null))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("대여 가능 여부 확인")
    class ValidationTest {

        @Test
        @DisplayName("대여 가능 (최대 권수 미만)")
        void canMemberLoan_가능() {
            // Given
            given(memberRepository.findMemberById(1L)).willReturn(testMember);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L))
                    .willReturn(List.of(testLoan, testLoan, testLoan));

            // When
            boolean result = loanService.canMemberLoan(1L);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("대여 불가능 (최대 권수 초과)")
        void canMemberLoan_불가능() {
            // Given
            given(memberRepository.findMemberById(1L)).willReturn(testMember);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L))
                    .willReturn(List.of(testLoan, testLoan, testLoan, testLoan, testLoan));

            // When
            boolean result = loanService.canMemberLoan(1L);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("도서 대여 가능 여부 확인 - 가능")
        void isBookAvailableForLoan_가능() {
            // Given
            testBook.setAvailable(true);
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));

            // When
            boolean result = loanService.isBookAvailableForLoan(1L);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("도서 대여 가능 여부 확인 - 불가능")
        void isBookAvailableForLoan_불가능() {
            // Given
            testBook.setAvailable(false);
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));

            // When
            boolean result = loanService.isBookAvailableForLoan(1L);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 도서 확인 시 예외")
        void isBookAvailableForLoan_도서없음() {
            // Given
            given(bookRepository.findById(any())).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> loanService.isBookAvailableForLoan(1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("특수 조회 기능")
    class SpecialSearchTest {

        @Test
        @DisplayName("연체된 대여 목록 조회")
        void getOverdueLoans_성공() {
            // Given
            Loan overdueLoan = Loan.builder()
                    .id(2L)
                    .book(testBook)
                    .member(testMember)
                    .dueDate(LocalDateTime.now().minusDays(1))
                    .build();
            given(loanRepository.findOverdueLoans(any(LocalDateTime.class)))
                    .willReturn(List.of(overdueLoan));

            // When
            List<LoanResponse> results = loanService.getOverdueLoans();

            // Then
            assertThat(results).isNotEmpty();
            verify(loanRepository).findOverdueLoans(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("날짜 범위로 대여 조회")
        void getLoansByDateRange_성공() {
            // Given
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();
            given(loanRepository.findByLoanDateBetween(start, end))
                    .willReturn(List.of(testLoan));

            // When
            List<LoanResponse> results = loanService.getLoansByDateRange(start, end);

            // Then
            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("회원 ID로 활성 대여 조회 - 성공")
        void getActiveLoansByMemberId_성공() {
            // Given
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L))
                    .willReturn(List.of(testLoan));

            // When
            List<LoanResponse> results = loanService.getActiveLoansByMemberId(1L);

            // Then
            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("회원 ID로 활성 대여 조회 - 회원 없음")
        void getActiveLoansByMemberId_회원없음() {
            // Given
            // getActiveLoansByMemberId 구현에서 memberId null 체크가 있다면 해당 로직 테스트
            // 여기서는 서비스 구현상 memberId가 null이면 예외를 던지는 로직을 테스트
            assertThatThrownBy(() -> loanService.getActiveLoansByMemberId(null))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
