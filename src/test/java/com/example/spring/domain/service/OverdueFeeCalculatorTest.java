package com.example.spring.domain.service;

import com.example.spring.domain.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OverdueFeeCalculator 테스트")
class OverdueFeeCalculatorTest {

    private OverdueFeeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new OverdueFeeCalculator();
    }

    @Nested
    @DisplayName("연체 일수 계산")
    class CalculateOverdueDaysTest {

        @Test
        @DisplayName("연체되지 않은 경우 0일 반환")
        void notOverdue_returnsZero() {
            LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
            LocalDateTime returnDate = LocalDateTime.now();

            long days = calculator.calculateOverdueDays(dueDate, returnDate);

            assertThat(days).isZero();
        }

        @Test
        @DisplayName("반납 예정일에 반납한 경우 0일 반환")
        void returnOnDueDate_returnsZero() {
            LocalDateTime dueDate = LocalDateTime.now();
            LocalDateTime returnDate = LocalDateTime.now();

            long days = calculator.calculateOverdueDays(dueDate, returnDate);

            assertThat(days).isZero();
        }

        @Test
        @DisplayName("3일 연체된 경우 3일 반환")
        void overdue3Days_returns3() {
            LocalDateTime dueDate = LocalDateTime.now().minusDays(3);
            LocalDateTime returnDate = LocalDateTime.now();

            long days = calculator.calculateOverdueDays(dueDate, returnDate);

            assertThat(days).isEqualTo(3);
        }

        @Test
        @DisplayName("반납일이 null이면 현재 시간 기준 계산")
        void nullReturnDate_usesCurrentTime() {
            LocalDateTime dueDate = LocalDateTime.now().minusDays(5);

            long days = calculator.calculateOverdueDays(dueDate, null);

            assertThat(days).isGreaterThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("연체료 계산")
    class CalculateTest {

        @Test
        @DisplayName("연체되지 않은 경우 0원 반환")
        void notOverdue_returnsZero() {
            LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
            LocalDateTime returnDate = LocalDateTime.now();

            Money fee = calculator.calculate(dueDate, returnDate);

            assertThat(fee.isZero()).isTrue();
        }

        @Test
        @DisplayName("1일 연체 시 1,000원 반환")
        void overdue1Day_returns1000() {
            LocalDateTime dueDate = LocalDateTime.now().minusDays(1);
            LocalDateTime returnDate = LocalDateTime.now();

            Money fee = calculator.calculate(dueDate, returnDate);

            assertThat(fee).isEqualTo(Money.of(1000));
        }

        @Test
        @DisplayName("10일 연체 시 10,000원 반환")
        void overdue10Days_returns10000() {
            LocalDateTime dueDate = LocalDateTime.now().minusDays(10);
            LocalDateTime returnDate = LocalDateTime.now();

            Money fee = calculator.calculate(dueDate, returnDate);

            assertThat(fee).isEqualTo(Money.of(10000));
        }

        @Test
        @DisplayName("최대 연체료(30,000원)를 초과하지 않음")
        void maxOverdueFee_notExceeded() {
            LocalDateTime dueDate = LocalDateTime.now().minusDays(50);
            LocalDateTime returnDate = LocalDateTime.now();

            Money fee = calculator.calculate(dueDate, returnDate);

            assertThat(fee).isEqualTo(Money.of(30000));
        }
    }

    @Nested
    @DisplayName("도서 가격 기반 연체료 계산")
    class CalculateWithBookPriceLimitTest {

        @Test
        @DisplayName("도서 가격보다 낮은 연체료는 그대로 반환")
        void feeLowerThanBookPrice_returnsFee() {
            LocalDateTime dueDate = LocalDateTime.now().minusDays(5);
            LocalDateTime returnDate = LocalDateTime.now();
            Money bookPrice = Money.of(20000);

            Money fee = calculator.calculateWithBookPriceLimit(dueDate, returnDate, bookPrice);

            assertThat(fee).isEqualTo(Money.of(5000));
        }

        @Test
        @DisplayName("연체료가 도서 가격을 초과하면 도서 가격 반환")
        void feeHigherThanBookPrice_returnsBookPrice() {
            LocalDateTime dueDate = LocalDateTime.now().minusDays(50);
            LocalDateTime returnDate = LocalDateTime.now();
            Money bookPrice = Money.of(15000);

            Money fee = calculator.calculateWithBookPriceLimit(dueDate, returnDate, bookPrice);

            assertThat(fee).isEqualTo(bookPrice);
        }
    }

    @Nested
    @DisplayName("연체 여부 확인")
    class IsOverdueTest {

        @Test
        @DisplayName("반납 예정일 전이면 연체 아님")
        void beforeDueDate_notOverdue() {
            LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
            LocalDateTime returnDate = LocalDateTime.now();

            boolean isOverdue = calculator.isOverdue(dueDate, returnDate);

            assertThat(isOverdue).isFalse();
        }

        @Test
        @DisplayName("반납 예정일 후이면 연체")
        void afterDueDate_overdue() {
            LocalDateTime dueDate = LocalDateTime.now().minusDays(1);
            LocalDateTime returnDate = LocalDateTime.now();

            boolean isOverdue = calculator.isOverdue(dueDate, returnDate);

            assertThat(isOverdue).isTrue();
        }
    }

    @Test
    @DisplayName("일일 연체료 조회")
    void getDailyFee() {
        assertThat(calculator.getDailyFee()).isEqualTo(Money.of(1000));
    }

    @Test
    @DisplayName("최대 연체료 조회")
    void getMaxOverdueFee() {
        assertThat(calculator.getMaxOverdueFee()).isEqualTo(Money.of(30000));
    }
}