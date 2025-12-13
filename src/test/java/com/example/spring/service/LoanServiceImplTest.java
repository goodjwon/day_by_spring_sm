package com.example.spring.service;

import com.example.spring.dto.request.CreateLoanRequest;
import com.example.spring.entity.Member;
import com.example.spring.entity.MembershipType;
import com.example.spring.exception.BookException;
import com.example.spring.exception.EntityNotFoundException;
import com.example.spring.repository.BookRepository;
import com.example.spring.repository.LoanRepository;
import com.example.spring.repository.MemberRepository;
import com.example.spring.service.impl.LoanServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

    private CreateLoanRequest createLoanRequest;
    private Member testMember;

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

    }

    @Test
    @DisplayName("존재하지 않는 회원 조회")
    void creatLoan_존재하지_않는_회원예외발생() {
        //Given
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(()->loanService.createLoan(createLoanRequest))
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
}
