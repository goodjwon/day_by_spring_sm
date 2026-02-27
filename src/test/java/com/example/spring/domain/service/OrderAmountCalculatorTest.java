package com.example.spring.domain.service;

import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderAmountCalculator 테스트")
class OrderAmountCalculatorTest {

    private OrderAmountCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new OrderAmountCalculator();
    }

    @Nested
    @DisplayName("총 금액 계산")
    class CalculateTotalTest {

        @Test
        @DisplayName("단일 항목 계산")
        void singleItem() {
            OrderItem item = createOrderItem(10000, 2);

            Money total = calculator.calculateTotal(List.of(item));

            assertThat(total).isEqualTo(Money.of(20000));
        }

        @Test
        @DisplayName("여러 항목 합산")
        void multipleItems() {
            List<OrderItem> items = Arrays.asList(
                    createOrderItem(10000, 2),  // 20,000
                    createOrderItem(15000, 1),  // 15,000
                    createOrderItem(5000, 3)    // 15,000
            );

            Money total = calculator.calculateTotal(items);

            assertThat(total).isEqualTo(Money.of(50000));
        }

        @Test
        @DisplayName("빈 목록은 0원")
        void emptyList_returnsZero() {
            Money total = calculator.calculateTotal(Collections.emptyList());

            assertThat(total.isZero()).isTrue();
        }

        @Test
        @DisplayName("null 목록은 0원")
        void nullList_returnsZero() {
            Money total = calculator.calculateTotal(null);

            assertThat(total.isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("할인 적용")
    class ApplyDiscountTest {

        @Test
        @DisplayName("정상 할인 적용")
        void normalDiscount() {
            Money total = Money.of(50000);
            Money discount = Money.of(5000);

            Money result = calculator.applyDiscount(total, discount);

            assertThat(result).isEqualTo(Money.of(45000));
        }

        @Test
        @DisplayName("할인 없음 - null")
        void nullDiscount_noChange() {
            Money total = Money.of(50000);

            Money result = calculator.applyDiscount(total, null);

            assertThat(result).isEqualTo(total);
        }

        @Test
        @DisplayName("할인 없음 - 0원")
        void zeroDiscount_noChange() {
            Money total = Money.of(50000);

            Money result = calculator.applyDiscount(total, Money.zero());

            assertThat(result).isEqualTo(total);
        }

        @Test
        @DisplayName("할인이 총액 초과 시 0원")
        void discountExceedsTotal_returnsZero() {
            Money total = Money.of(5000);
            Money discount = Money.of(10000);

            Money result = calculator.applyDiscount(total, discount);

            assertThat(result.isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("퍼센트 할인 적용")
    class ApplyPercentDiscountTest {

        @Test
        @DisplayName("10% 할인")
        void tenPercent() {
            Money total = Money.of(50000);

            Money result = calculator.applyPercentDiscount(total, 10);

            assertThat(result).isEqualTo(Money.of(45000));
        }

        @Test
        @DisplayName("0% 할인")
        void zeroPercent_noChange() {
            Money total = Money.of(50000);

            Money result = calculator.applyPercentDiscount(total, 0);

            assertThat(result).isEqualTo(total);
        }

        @Test
        @DisplayName("100% 할인 - 0원")
        void hundredPercent_returnsZero() {
            Money total = Money.of(50000);

            Money result = calculator.applyPercentDiscount(total, 100);

            assertThat(result.isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("배송비 계산")
    class CalculateShippingFeeTest {

        @Test
        @DisplayName("30,000원 미만 - 배송비 3,000원")
        void belowThreshold_chargesShipping() {
            Money orderTotal = Money.of(25000);

            Money shippingFee = calculator.calculateShippingFee(orderTotal);

            assertThat(shippingFee).isEqualTo(Money.of(3000));
        }

        @Test
        @DisplayName("30,000원 이상 - 무료 배송")
        void atOrAboveThreshold_freeShipping() {
            Money orderTotal = Money.of(30000);

            Money shippingFee = calculator.calculateShippingFee(orderTotal);

            assertThat(shippingFee.isZero()).isTrue();
        }

        @Test
        @DisplayName("50,000원 - 무료 배송")
        void wellAboveThreshold_freeShipping() {
            Money orderTotal = Money.of(50000);

            Money shippingFee = calculator.calculateShippingFee(orderTotal);

            assertThat(shippingFee.isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("포인트 계산")
    class PointsCalculationTest {

        @Test
        @DisplayName("적립 포인트 계산 (1%)")
        void calculatePointsToEarn() {
            Money paymentAmount = Money.of(50000);

            int points = calculator.calculatePointsToEarn(paymentAmount);

            assertThat(points).isEqualTo(500);
        }

        @Test
        @DisplayName("포인트 사용 적용")
        void applyPoints() {
            Money total = Money.of(50000);
            int points = 5000;

            Money result = calculator.applyPoints(total, points);

            assertThat(result).isEqualTo(Money.of(45000));
        }

        @Test
        @DisplayName("포인트 0 사용 - 변경 없음")
        void zeroPoints_noChange() {
            Money total = Money.of(50000);

            Money result = calculator.applyPoints(total, 0);

            assertThat(result).isEqualTo(total);
        }
    }

    @Nested
    @DisplayName("최종 금액 계산")
    class CalculateFinalAmountTest {

        @Test
        @DisplayName("할인 + 무료배송")
        void withDiscountAndFreeShipping() {
            List<OrderItem> items = List.of(
                    createOrderItem(20000, 1),
                    createOrderItem(15000, 1)
            );
            Money discount = Money.of(5000);

            Money finalAmount = calculator.calculateFinalAmount(items, discount);

            // 35,000 - 5,000 = 30,000 (배송비 무료)
            assertThat(finalAmount).isEqualTo(Money.of(30000));
        }

        @Test
        @DisplayName("할인 + 배송비")
        void withDiscountAndShipping() {
            List<OrderItem> items = List.of(createOrderItem(20000, 1));
            Money discount = Money.of(2000);

            Money finalAmount = calculator.calculateFinalAmount(items, discount);

            // 20,000 - 2,000 + 3,000(배송비) = 21,000
            assertThat(finalAmount).isEqualTo(Money.of(21000));
        }
    }

    @Test
    @DisplayName("설정값 조회")
    void getSettings() {
        assertThat(calculator.getFreeShippingThreshold()).isEqualTo(Money.of(30000));
        assertThat(calculator.getDefaultShippingFee()).isEqualTo(Money.of(3000));
        assertThat(calculator.getPointsRate()).isEqualTo(0.01);
    }

    // 헬퍼 메서드
    private OrderItem createOrderItem(int price, int quantity) {
        return OrderItem.builder()
                .price(Money.of(price))
                .quantity(quantity)
                .build();
    }
}