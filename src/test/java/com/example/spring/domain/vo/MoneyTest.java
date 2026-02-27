package com.example.spring.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Nested
    @DisplayName("Money 생성")
    class Creation {

        @Test
        @DisplayName("BigDecimal 값으로 Money 생성")
        void createWithBigDecimal() {
            Money money = Money.of(new BigDecimal("10000.00"));

            assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("10000.00"));
            assertThat(money.getCurrency()).isEqualTo("KRW");
        }

        @Test
        @DisplayName("long 값으로 Money 생성")
        void createWithLong() {
            Money money = Money.of(10000);

            assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("10000.00"));
            assertThat(money.getCurrency()).isEqualTo("KRW");
        }

        @Test
        @DisplayName("통화 지정하여 Money 생성")
        void createWithCurrency() {
            Money money = Money.of(new BigDecimal("100.00"), "USD");

            assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(money.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("zero() 메서드로 0원 Money 생성")
        void createZero() {
            Money money = Money.zero();

            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(money.isZero()).isTrue();
        }

        @Test
        @DisplayName("null 금액으로 생성 시 예외 발생")
        void createWithNullAmount() {
            assertThatThrownBy(() -> Money.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("금액은 null일 수 없습니다");
        }

        @Test
        @DisplayName("유효하지 않은 통화 코드로 생성 시 예외 발생")
        void createWithInvalidCurrency() {
            assertThatThrownBy(() -> Money.of(BigDecimal.TEN, "KRWW"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("통화 코드는 3자리여야 합니다");
        }
    }

    @Nested
    @DisplayName("Money 연산")
    class Operations {

        @Test
        @DisplayName("두 Money 더하기")
        void addTwoMoney() {
            Money money1 = Money.of(10000);
            Money money2 = Money.of(5000);

            Money result = money1.add(money2);

            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("15000.00"));
        }

        @Test
        @DisplayName("두 Money 빼기")
        void subtractTwoMoney() {
            Money money1 = Money.of(10000);
            Money money2 = Money.of(3000);

            Money result = money1.subtract(money2);

            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("7000.00"));
        }

        @Test
        @DisplayName("정수와 곱하기")
        void multiplyByInt() {
            Money money = Money.of(5000);

            Money result = money.multiply(3);

            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("15000.00"));
        }

        @Test
        @DisplayName("다른 통화끼리 연산 시 예외 발생")
        void operationWithDifferentCurrency() {
            Money krw = Money.of(10000, "KRW");
            Money usd = Money.of(100, "USD");

            assertThatThrownBy(() -> krw.add(usd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("통화가 다릅니다");
        }
    }

    @Nested
    @DisplayName("Money 비교")
    class Comparison {

        @Test
        @DisplayName("isGreaterThan 테스트")
        void isGreaterThan() {
            Money money1 = Money.of(10000);
            Money money2 = Money.of(5000);

            assertThat(money1.isGreaterThan(money2)).isTrue();
            assertThat(money2.isGreaterThan(money1)).isFalse();
        }

        @Test
        @DisplayName("isLessThan 테스트")
        void isLessThan() {
            Money money1 = Money.of(5000);
            Money money2 = Money.of(10000);

            assertThat(money1.isLessThan(money2)).isTrue();
            assertThat(money2.isLessThan(money1)).isFalse();
        }

        @Test
        @DisplayName("isPositive 테스트")
        void isPositive() {
            assertThat(Money.of(1000).isPositive()).isTrue();
            assertThat(Money.zero().isPositive()).isFalse();
        }

        @Test
        @DisplayName("equals 및 hashCode 테스트")
        void equalsAndHashCode() {
            Money money1 = Money.of(10000);
            Money money2 = Money.of(10000);
            Money money3 = Money.of(5000);

            assertThat(money1).isEqualTo(money2);
            assertThat(money1).isNotEqualTo(money3);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }
    }

    @Test
    @DisplayName("toString 테스트")
    void toStringTest() {
        Money money = Money.of(10000);

        assertThat(money.toString()).isEqualTo("10000.00 KRW");
    }
}