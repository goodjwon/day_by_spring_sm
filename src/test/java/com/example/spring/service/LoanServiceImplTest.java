package com.example.spring.service;

import com.example.spring.dto.request.CreateLoanRequest;
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

//            Loan overDueLoan = new Loan();
//            overDueLoan.setDueDate(LocalDateTime.now().minusDays(3));
//            overDueLoan.setStatus(LoanStatus.OVERDUE);

            given(loanRepository.existsByMemberAndStatus(testMember, LoanStatus.OVERDUE))
                    .willReturn(true);
            //When
            //Then
            assertThatThrownBy(() -> loanService.createLoan(createLoanRequest))
                    .isInstanceOf(LoanException.OverdueLoansExistException.class);
        }
    }
}
