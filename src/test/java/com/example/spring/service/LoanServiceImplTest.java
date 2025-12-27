package com.example.spring.service;

import com.example.spring.dto.request.CreateLoanRequest;
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
                .title("test book")
                .author("test author")
                .isbn("123412341234")
                .price(new BigDecimal(40000))
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
    @DisplayName("대여 생성")
    class creatLoanTest {

        @Test
        @DisplayName("존재하지 않는 회원 조회")
        void creatLoan_존재하지_않는_회원예외발생() {
            //Given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            //When & Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("회원을 찾을 수 없습니다");

        }

        @Test
        @DisplayName("존재하지 않는 도서 조회")
        void creatLoan_존재하지_않는_도서예외발생() {
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
        void creatLoan_도서재고부족() {
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
        void creatLoan_대여중인_도서대여시도() {
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
        void creatLoan_연체여부확인() {
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
        void creatLoan_대여생성성공() {
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
    }

    @Test
    @DisplayName("대여 목록 페이징 조회")
    void getAllLoansWithPagination() {
        //Given
        Pageable pageable = PageRequest.of(0,10);
        Page<Loan> page = new PageImpl<>(List.of(testLoan));
        given(loanRepository.findAll(any(Pageable.class))).willReturn(page);

        //When
        Page<LoanResponse> result = loanService.getAllLoansWithPagination(pageable, null, null);

        //Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBook().getTitle()).isEqualTo(testBook.getTitle());
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
        assertThat(result.get().getMember().getName()).isEqualTo("홍길동");
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
    @DisplayName("대여 정보 수정 - 성공")
    void updateLoan_조회_성공() {
        //Given
        LocalDateTime returnDay = LocalDateTime.now().plusDays(7);
        UpdateLoanRequest savedLoan = UpdateLoanRequest.builder()
                .dueDate(returnDay)
                .status(LoanStatus.RETURNED)
                .build();
        given(loanRepository.findById(1L)).willReturn(Optional.of(testLoan));

        //When
        LoanResponse response = loanService.updateLoan(testLoan.getId(), savedLoan);

        //Then
        assertThat(response.getDueDate()).isEqualTo(returnDay);
        assertThat(response.getStatus()).isEqualTo(LoanStatus.RETURNED);
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
                .isInstanceOf(EntityNotFoundException.class);
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
