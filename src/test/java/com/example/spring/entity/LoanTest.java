package com.example.spring.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;



@DisplayName("Loan 엔티티 테스트")
class LoanTest {

    private Validator validator;
    private Member testMember;
    private Book testBook;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        // 테스트용 Member 생성
        testMember = Member.builder()
                .id(1L)
                .name("홍길동")
                .email("hong@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();

        // 테스트용 Book 생성
        testBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("45000"))
                .available(true)
                .build();
    }

    @Nested
    @DisplayName("Loan 생성 테스트")
    class CreateLoanTest {

        @Test
        @DisplayName("유효한 Loan 객체 생성 성공")
        void createValidLoan() {
            // given
            LocalDateTime loanDate = LocalDateTime.now();
            LocalDateTime dueDate = loanDate.plusDays(14);

            // when
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(loanDate)
                    .dueDate(dueDate)
                    .status(LoanStatus.ACTIVE)
                    .build();

            Set<ConstraintViolation<Loan>> violations = validator.validate(loan);

            // then
            assertThat(violations).isEmpty();
            assertThat(loan.getMember()).isEqualTo(testMember);
            assertThat(loan.getBook()).isEqualTo(testBook);
            assertThat(loan.getLoanDate()).isEqualTo(loanDate);
            assertThat(loan.getDueDate()).isEqualTo(dueDate);
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE); // Default value
            assertThat(loan.getOverdueFee()).isEqualByComparingTo(BigDecimal.ZERO); // Default value
        }

        @Test
        @DisplayName("Builder 패턴으로 Loan 생성")
        void createLoanWithBuilder() {
            // given & when
            LocalDateTime loanDate = LocalDateTime.now();
            LocalDateTime dueDate = loanDate.plusDays(14);
            LocalDateTime createdDate = LocalDateTime.now();

            Loan loan = Loan.builder()
                    .id(1L)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(loanDate)
                    .dueDate(dueDate)
                    .status(LoanStatus.ACTIVE)
                    .overdueFee(BigDecimal.ZERO)
                    .createdDate(createdDate)
                    .build();

            // then
            assertThat(loan.getId()).isEqualTo(1L);
            assertThat(loan.getMember()).isEqualTo(testMember);
            assertThat(loan.getBook()).isEqualTo(testBook);
            assertThat(loan.getLoanDate()).isEqualTo(loanDate);
            assertThat(loan.getDueDate()).isEqualTo(dueDate);
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
            assertThat(loan.getCreatedDate()).isEqualTo(createdDate);
        }

        @Test
        @DisplayName("기본값 설정 확인")
        void checkDefaultValues() {
            // given & when
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now())
                    .status(LoanStatus.ACTIVE)
                    .dueDate(LocalDateTime.now().plusDays(14))

                    .build();

            // then
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
            assertThat(loan.getOverdueFee()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(loan.getReturnDate()).isNull();
        }
    }

    @Nested
    @DisplayName("Bean Validation 테스트")
    class ValidationTest {

        @Test
        @DisplayName("회원 정보가 null이면 검증 실패")
        void memberShouldNotBeNull() {
            // given
            Loan loan = Loan.builder()
                    .member(null)
                    .book(testBook)
                    .loanDate(LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(14))
                    .build();

            // when
            Set<ConstraintViolation<Loan>> violations = validator.validate(loan);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("회원 정보는 필수입니다");
        }

        @Test
        @DisplayName("도서 정보가 null이면 검증 실패")
        void bookShouldNotBeNull() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(null)
                    .loanDate(LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(14))
                    .build();

            // when
            Set<ConstraintViolation<Loan>> violations = validator.validate(loan);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("도서 정보는 필수입니다");
        }

        @Test
        @DisplayName("대여일자가 null이면 검증 실패")
        void loanDateShouldNotBeNull() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(null)
                    .dueDate(LocalDateTime.now().plusDays(14))
                    .build();

            // when
            Set<ConstraintViolation<Loan>> violations = validator.validate(loan);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("대여일자는 필수입니다");
        }

        @Test
        @DisplayName("반납예정일이 null이면 검증 실패")
        void dueDateShouldNotBeNull() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now())
                    .dueDate(null)
                    .build();

            // when
            Set<ConstraintViolation<Loan>> violations = validator.validate(loan);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("반납예정일은 필수입니다");
        }
    }

    @Nested
    @DisplayName("연체 여부 확인 테스트")
    class IsOverdueTest {

        @Test
        @DisplayName("반납예정일이 지나지 않은 경우 - 연체 아님")
        void notOverdueWhenBeforeDueDate() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(LocalDateTime.now().plusDays(4))
                    .build();

            // when & then
            assertThat(loan.isOverdue()).isFalse();
        }

        @Test
        @DisplayName("반납예정일이 지난 경우 - 연체")
        void overdueWhenAfterDueDate() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(20))
                    .dueDate(LocalDateTime.now().minusDays(6))
                    .build();

            // when & then
            assertThat(loan.isOverdue()).isTrue();
        }

        @Test
        @DisplayName("이미 반납된 경우 - 연체 아님")
        void notOverdueWhenAlreadyReturned() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(20))
                    .dueDate(LocalDateTime.now().minusDays(6))
                    .returnDate(LocalDateTime.now().minusDays(5))
                    .build();

            // when & then
            assertThat(loan.isOverdue()).isFalse();
        }

        @Test
        @DisplayName("반납예정일이 1시간 후 - 연체 아님")
        void notOverdueWhenDueDateIsInFuture() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(now.minusDays(14))
                    .dueDate(now.plusHours(1))
                    .build();

            // when & then
            assertThat(loan.isOverdue()).isFalse();
        }
    }

    @Nested
    @DisplayName("연체 일수 계산 테스트")
    class GetOverdueDaysTest {

        @Test
        @DisplayName("연체되지 않은 경우 - 0일 반환")
        void returnZeroWhenNotOverdue() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(LocalDateTime.now().plusDays(4))
                    .build();

            // when & then
            assertThat(loan.getOverdueDays()).isEqualTo(0);
        }

        @Test
        @DisplayName("연체된 경우 - 정확한 연체 일수 반환")
        void returnCorrectOverdueDays() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(now.minusDays(20))
                    .dueDate(now.minusDays(5))
                    .build();

            // when
            long overdueDays = loan.getOverdueDays();

            // then
            assertThat(overdueDays).isGreaterThanOrEqualTo(4);
            assertThat(overdueDays).isLessThanOrEqualTo(5);
        }

        @Test
        @DisplayName("이미 반납된 경우 - 0일 반환")
        void returnZeroWhenAlreadyReturned() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(20))
                    .dueDate(LocalDateTime.now().minusDays(6))
                    .returnDate(LocalDateTime.now().minusDays(5))
                    .build();

            // when & then
            assertThat(loan.getOverdueDays()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("연체료 계산 테스트")
    class CalculateOverdueFeeTest {

        private final LocalDateTime baseTime = LocalDateTime.now();

        @Test
        @DisplayName("연체되지 않은 경우 - 연체료 0원")
        void returnZeroFeeWhenNotOverdue() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(LocalDateTime.now().plusDays(4))
                    .build();

            // when
            BigDecimal fee = loan.calculateOverdueFee(baseTime);

            // then
            assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("5일 연체 - 연체료 5000원")
        void returnCorrectFeeFor5DaysOverdue() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(now.minusDays(20))
                    .dueDate(now.minusDays(5))
                    .build();

            // when
            BigDecimal fee = loan.calculateOverdueFee(baseTime);

            // then - 4일 ~ 5일 사이의 연체료 (4000 ~ 5000원)
            assertThat(fee).isGreaterThanOrEqualTo(new BigDecimal("4000"));
            assertThat(fee).isLessThanOrEqualTo(new BigDecimal("5000"));
        }

        @Test
        @DisplayName("1일 연체 - 연체료 1000원")
        void returnCorrectFeeFor1DayOverdue() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(now.minusDays(16))
                    .dueDate(now.minusDays(1))
                    .build();

            // when
            BigDecimal fee = loan.calculateOverdueFee(baseTime);

            // then - 0일 ~ 1일 사이의 연체료 (0 ~ 1000원)
            assertThat(fee).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(fee).isLessThanOrEqualTo(new BigDecimal("1000"));
        }

        @Test
        @DisplayName("이미 반납된 경우 - 연체료 0원")
        void returnZeroFeeWhenAlreadyReturned() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(20))
                    .dueDate(LocalDateTime.now().minusDays(6))
                    .returnDate(LocalDateTime.now().minusDays(5))
                    .build();

            // when
            BigDecimal fee = loan.calculateOverdueFee(baseTime);

            // then
            assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("도서 반납 테스트")
    class ReturnBookTest {

        @Test
        @DisplayName("정상 반납 - 반납일시 설정 및 상태 변경")
        void returnBookSuccessfully() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(LocalDateTime.now().plusDays(4))
                    .build();

            // when
            loan.returnBook();

            // then
            assertThat(loan.getReturnDate()).isNotNull();
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
            assertThat(loan.getOverdueFee()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("연체 후 반납 - 연체료 자동 계산")
        void returnBookWithOverdueFee() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(now.minusDays(20))
                    .dueDate(now.minusDays(5))
                    .build();

            // when
            loan.updateStatus(now);

            // then
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.OVERDUE);
            assertThat(loan.getOverdueFee()).isEqualByComparingTo(new BigDecimal("5000"));
        }

        @Test
        @DisplayName("반납 시 현재 시간으로 반납일시 설정")
        void returnDateShouldBeNow() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(LocalDateTime.now().plusDays(4))
                    .build();

            LocalDateTime beforeReturn = LocalDateTime.now();

            // when
            loan.returnBook();

            // then
            LocalDateTime afterReturn = LocalDateTime.now();
            assertThat(loan.getReturnDate()).isBetween(beforeReturn, afterReturn);
        }
    }

    @Nested
    @DisplayName("대여 연장 테스트")
    class ExtendLoanTest {

        @Test
        @DisplayName("정상 연장 - 반납예정일 연장")
        void extendLoanSuccessfully() {
            // given
            LocalDateTime originalDueDate = LocalDateTime.now().plusDays(4);
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(originalDueDate)
                    .build();

            // when
            loan.extendLoan(7, originalDueDate);

            // then
            assertThat(loan.getDueDate()).isEqualTo(originalDueDate.plusDays(7));
        }

        @Test
        @DisplayName("이미 반납된 대여 연장 시도 - 예외 발생")
        void throwExceptionWhenExtendingReturnedLoan() {
            // given
            LocalDateTime originalDueDate = LocalDateTime.now().minusDays(4);
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(originalDueDate.plusDays(4))
                    .returnDate(LocalDateTime.now().minusDays(1))
                    .build();

            // when & then
            assertThatThrownBy(() -> loan.extendLoan(7, originalDueDate))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 반납된 대여는 연장할 수 없습니다");
        }

        @Test
        @DisplayName("연체된 대여 연장 시도 - 예외 발생")
        void throwExceptionWhenExtendingOverdueLoan() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(20))
                    .dueDate(LocalDateTime.now().minusDays(5))
                    .build();

            // when & then
            assertThatThrownBy(() -> loan.extendLoan(7, LocalDateTime.now()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("연체된 대여는 연장할 수 없습니다");
        }

        @Test
        @DisplayName("14일 연장")
        void extend14Days() {
            // given
            LocalDateTime originalDueDate = LocalDateTime.now().plusDays(4);
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(originalDueDate)
                    .build();

            // when
            loan.extendLoan(14,  originalDueDate);

            // then
            assertThat(loan.getDueDate()).isEqualTo(originalDueDate.plusDays(14));
        }
    }

    @Nested
    @DisplayName("대여 취소 테스트")
    class CancelTest {

        @Test
        @DisplayName("정상 취소 - 상태 변경")
        void cancelLoanSuccessfully() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(14))
                    .build();

            // when
            loan.cancelLoan();

            // then
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.CANCELLED);
            assertThat(loan.getReturnDate()).isNull();
        }

        @Test
        @DisplayName("이미 반납된 대여 취소 시도 - 예외 발생")
        void throwExceptionWhenCancellingReturnedLoan() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(LocalDateTime.now().plusDays(4))
                    .returnDate(LocalDateTime.now())
                    .build();

            // when & then
            assertThatThrownBy(() -> loan.cancelLoan())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 반납된 대여는 취소할 수 없습니다");
        }

        @Test
        @DisplayName("연체된 대여도 취소 가능")
        void canCancelOverdueLoan() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(20))
                    .dueDate(LocalDateTime.now().minusDays(5))
                    .build();

            // when
            loan.cancelLoan();

            // then
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("대여 상태 업데이트 테스트")
    class UpdateStatusTest {

        @Test
        @DisplayName("정상 대여 - ACTIVE 상태 유지")
        void updateToActiveWhenNormal() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(LocalDateTime.now().plusDays(4))
                    .build();

            // when
            loan.updateStatus();

            // then
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
            assertThat(loan.getOverdueFee()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("연체 발생 - OVERDUE 상태로 변경 및 연체료 계산")
        void updateToOverdueWhenPastDueDate() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(20))
                    .dueDate(LocalDateTime.now().minusDays(5))
                    .status(LoanStatus.ACTIVE)
                    .build();

            // when
            loan.updateStatus();

            // then
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.OVERDUE);
            assertThat(loan.getOverdueFee()).isEqualByComparingTo(new BigDecimal("5000"));
        }

        @Test
        @DisplayName("반납 완료 - RETURNED 상태로 변경")
        void updateToReturnedWhenReturned() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(LocalDateTime.now().plusDays(4))
                    .returnDate(LocalDateTime.now())
                    .status(LoanStatus.ACTIVE)
                    .build();

            // when
            loan.updateStatus(LocalDateTime.now());

            // then
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
            assertThat(loan.getOverdueFee()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("OVERDUE에서 ACTIVE로 상태 변경 안됨 (returnDate 있을 때)")
        void notChangeFromOverdueToActive() {
            // given
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(LocalDateTime.now().plusDays(4))
                    .returnDate(LocalDateTime.now())
                    .status(LoanStatus.OVERDUE)
                    .overdueFee(new BigDecimal("5000"))
                    .build();

            // when
            loan.updateStatus();

            // then
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        }
    }

    @Nested
    @DisplayName("Audit 기능 테스트")
    class AuditTest {

        @Test
        @DisplayName("생성 시간과 수정 시간 필드 존재 확인")
        void auditFieldsExist() {
            // given & when
            Loan loan = Loan.builder()
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(14))
                    .createdDate(LocalDateTime.now())
                    .updatedDate(LocalDateTime.now())
                    .build();

            // then
            assertThat(loan.getCreatedDate()).isNotNull();
            assertThat(loan.getUpdatedDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("동등성 및 해시코드 테스트")
    class EqualsAndHashCodeTest {

        @Test
        @DisplayName("같은 ID를 가진 Loan 객체는 동등함")
        void loansWithSameIdShouldBeEqual() {
            // given
            LocalDateTime loanDate = LocalDateTime.now();
            LocalDateTime dueDate = loanDate.plusDays(14);

            Loan loan1 = Loan.builder()
                    .id(1L)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(loanDate)
                    .dueDate(dueDate)
                    .build();

            Loan loan2 = Loan.builder()
                    .id(1L)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(loanDate)
                    .dueDate(dueDate)
                    .build();

            // when & then
            assertThat(loan1).isEqualTo(loan2);
            assertThat(loan1.hashCode()).isEqualTo(loan2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 Loan 객체는 다름")
        void loansWithDifferentIdShouldNotBeEqual() {
            // given
            LocalDateTime loanDate = LocalDateTime.now();
            LocalDateTime dueDate = loanDate.plusDays(14);

            Loan loan1 = Loan.builder()
                    .id(1L)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(loanDate)
                    .dueDate(dueDate)
                    .build();

            Loan loan2 = Loan.builder()
                    .id(2L)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(loanDate)
                    .dueDate(dueDate)
                    .build();

            // when & then
            assertThat(loan1).isNotEqualTo(loan2);
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString 메서드가 주요 필드를 포함함")
        void toStringShouldContainMainFields() {
            // given
            Loan loan = Loan.builder()
                    .id(1L)
                    .member(testMember)
                    .book(testBook)
                    .loanDate(LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(14))
                    .status(LoanStatus.ACTIVE)
                    .build();

            // when
            String loanString = loan.toString();

            // then
            assertThat(loanString).contains("ACTIVE");
        }
    }
    }