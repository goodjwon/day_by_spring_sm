package com.example.spring.domain.service;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.*;
import com.example.spring.exception.LoanException;
import com.example.spring.domain.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanEligibilityChecker 테스트")
class LoanEligibilityCheckerTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanEligibilityChecker checker;

    private Member testMember;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .name("테스트 회원")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .build();

        testBook = Book.builder()
                .id(1L)
                .title("테스트 도서")
                .author("저자")
                .isbn(ISBN.of("9780132350884"))
                .price(Money.of(20000))
                .available(true)
                .build();
    }

    @Nested
    @DisplayName("도서 대출 가능 여부 검증")
    class ValidateBookAvailabilityTest {

        @Test
        @DisplayName("대출 가능한 도서 - 성공")
        void availableBook_success() {
            given(loanRepository.existsByBookIdAndReturnDateIsNull(testBook.getId())).willReturn(false);

            checker.validateBookAvailability(testBook);
            // 예외가 발생하지 않으면 성공
        }

        @Test
        @DisplayName("도서가 대출 불가 상태 - BookNotAvailableException")
        void unavailableBook_throwsException() {
            testBook.loanOut();

            assertThatThrownBy(() -> checker.validateBookAvailability(testBook))
                    .isInstanceOf(LoanException.BookNotAvailableException.class);
        }

        @Test
        @DisplayName("이미 대출 중인 도서 - BookAlreadyLoanedException")
        void alreadyLoaned_throwsException() {
            given(loanRepository.existsByBookIdAndReturnDateIsNull(testBook.getId())).willReturn(true);

            assertThatThrownBy(() -> checker.validateBookAvailability(testBook))
                    .isInstanceOf(LoanException.BookAlreadyLoanedException.class);
        }
    }

    @Nested
    @DisplayName("회원 대출 자격 검증")
    class ValidateMemberEligibilityTest {

        @Test
        @DisplayName("대출 자격 있는 회원 - 성공")
        void eligibleMember_success() {
            given(loanRepository.findByMemberIdAndReturnDateIsNull(testMember.getId()))
                    .willReturn(new ArrayList<>());

            checker.validateMemberEligibility(testMember);
            // 예외가 발생하지 않으면 성공
        }

        @Test
        @DisplayName("대출 한도 초과 - LoanLimitExceededException")
        void exceedsLoanLimit_throwsException() {
            List<Loan> activeLoans = createActiveLoans(5);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(testMember.getId()))
                    .willReturn(activeLoans);

            assertThatThrownBy(() -> checker.validateMemberEligibility(testMember))
                    .isInstanceOf(LoanException.LoanLimitExceededException.class);
        }

        @Test
        @DisplayName("연체 도서 보유 - OverdueLoansExistException")
        void hasOverdueLoans_throwsException() {
            Loan overdueLoan = createOverdueLoan();
            given(loanRepository.findByMemberIdAndReturnDateIsNull(testMember.getId()))
                    .willReturn(List.of(overdueLoan));

            assertThatThrownBy(() -> checker.validateMemberEligibility(testMember))
                    .isInstanceOf(LoanException.OverdueLoansExistException.class);
        }
    }

    @Nested
    @DisplayName("canLoan - boolean 반환")
    class CanLoanTest {

        @Test
        @DisplayName("모든 조건 충족 - true")
        void allConditionsMet_returnsTrue() {
            given(loanRepository.existsByBookIdAndReturnDateIsNull(testBook.getId())).willReturn(false);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(testMember.getId()))
                    .willReturn(new ArrayList<>());

            boolean result = checker.canLoan(testMember, testBook);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("도서 대출 불가 - false")
        void bookUnavailable_returnsFalse() {
            testBook.loanOut();

            boolean result = checker.canLoan(testMember, testBook);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("대출 현황 조회")
    class LoanStatusTest {

        @Test
        @DisplayName("현재 대출 수 조회")
        void getCurrentLoanCount() {
            List<Loan> activeLoans = createActiveLoans(3);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(testMember.getId()))
                    .willReturn(activeLoans);

            int count = checker.getCurrentLoanCount(testMember);

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("남은 대출 가능 수 조회")
        void getRemainingLoanCount() {
            List<Loan> activeLoans = createActiveLoans(3);
            given(loanRepository.findByMemberIdAndReturnDateIsNull(testMember.getId()))
                    .willReturn(activeLoans);

            int remaining = checker.getRemainingLoanCount(testMember);

            assertThat(remaining).isEqualTo(2); // 최대 5 - 현재 3 = 2
        }

        @Test
        @DisplayName("최대 대출 가능 수 조회")
        void getMaxLoanCount() {
            assertThat(checker.getMaxLoanCount()).isEqualTo(5);
        }
    }

    // 헬퍼 메서드
    private List<Loan> createActiveLoans(int count) {
        List<Loan> loans = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            loans.add(Loan.builder()
                    .id((long) i)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(14))
                    .status(LoanStatus.ACTIVE)
                    .build());
        }
        return loans;
    }

    private Loan createOverdueLoan() {
        return Loan.builder()
                .id(1L)
                .member(testMember)
                .book(testBook)
                .loanDate(LocalDateTime.now().minusDays(20))
                .dueDate(LocalDateTime.now().minusDays(6)) // 6일 전이 반납 예정일
                .status(LoanStatus.OVERDUE)
                .build();
    }
}