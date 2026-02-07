package com.example.spring.entity;

import com.example.spring.domain.vo.Money;
import com.example.spring.exception.OrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order 엔티티 테스트")
class OrderTest {

    private Member testMember;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .name("테스트 회원")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        testOrder = Order.builder()
                .id(1L)
                .member(testMember)
                .totalAmount(Money.of(new BigDecimal("50000")))
                .discountAmount(Money.zero())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("주문 생성")
    class CreateOrderTest {

        @Test
        @DisplayName("기본 주문 생성 - PENDING 상태")
        void createOrder_defaultStatus_pending() {
            // Given & When
            Order order = Order.builder()
                    .member(testMember)
                    .totalAmount(Money.of(new BigDecimal("30000")))
                    .orderDate(LocalDateTime.now())
                    .build();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getDiscountAmount()).isEqualTo(Money.zero());
        }

        @Test
        @DisplayName("할인 적용된 주문 생성")
        void createOrder_withDiscount_success() {
            // Given
            Money totalAmount = Money.of(new BigDecimal("50000"));
            Money discountAmount = Money.of(new BigDecimal("5000"));

            // When
            Order order = Order.builder()
                    .member(testMember)
                    .totalAmount(totalAmount)
                    .discountAmount(discountAmount)
                    .orderDate(LocalDateTime.now())
                    .build();

            // Then
            assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
            assertThat(order.getDiscountAmount().getAmount()).isEqualByComparingTo(new BigDecimal("5000"));
            assertThat(order.getFinalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("45000"));
        }
    }

    @Nested
    @DisplayName("주문 상태 전이")
    class OrderStatusTransitionTest {

        @Test
        @DisplayName("PENDING -> CONFIRMED 상태 전이")
        void confirm_fromPending_success() {
            // Given
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PENDING);

            // When
            testOrder.confirm();

            // Then
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(testOrder.getConfirmedDate()).isNotNull();
        }

        @Test
        @DisplayName("CONFIRMED -> SHIPPED 상태 전이")
        void ship_fromConfirmed_success() {
            // Given
            testOrder.confirm();
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            // When
            testOrder.ship();

            // Then
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(testOrder.getShippedDate()).isNotNull();
        }

        @Test
        @DisplayName("SHIPPED -> DELIVERED 상태 전이")
        void deliver_fromShipped_success() {
            // Given
            testOrder.confirm();
            testOrder.ship();
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);

            // When
            testOrder.deliver();

            // Then
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
            assertThat(testOrder.getDeliveredDate()).isNotNull();
        }

        @Test
        @DisplayName("PENDING 상태에서 배송 시작 시 예외")
        void ship_fromPending_throwsException() {
            // Given
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PENDING);

            // When & Then
            assertThatThrownBy(() -> testOrder.ship())
                    .isInstanceOf(OrderException.InvalidOrderStateException.class)
                    .hasMessageContaining("배송을 시작할 수 없는 주문");
        }

        @Test
        @DisplayName("PENDING 상태에서 배송 완료 시 예외")
        void deliver_fromPending_throwsException() {
            // Given
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PENDING);

            // When & Then
            assertThatThrownBy(() -> testOrder.deliver())
                    .isInstanceOf(OrderException.InvalidOrderStateException.class)
                    .hasMessageContaining("배송완료 처리할 수 없는 주문");
        }

        @Test
        @DisplayName("CONFIRMED 상태에서 다시 확인 시 예외")
        void confirm_fromConfirmed_throwsException() {
            // Given
            testOrder.confirm();

            // When & Then
            assertThatThrownBy(() -> testOrder.confirm())
                    .isInstanceOf(OrderException.InvalidOrderStateException.class)
                    .hasMessageContaining("확인할 수 없는 주문");
        }
    }

    @Nested
    @DisplayName("주문 취소")
    class CancelOrderTest {

        @Test
        @DisplayName("PENDING 상태 주문 취소 성공")
        void cancel_fromPending_success() {
            // Given
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PENDING);

            // When
            testOrder.cancel("고객 변심");

            // Then
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(testOrder.getCancelledDate()).isNotNull();
            assertThat(testOrder.getCancellationReason()).isEqualTo("고객 변심");
        }

        @Test
        @DisplayName("CONFIRMED 상태 주문 취소 성공")
        void cancel_fromConfirmed_success() {
            // Given
            testOrder.confirm();

            // When
            testOrder.cancel("재고 부족");

            // Then
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(testOrder.getCancellationReason()).isEqualTo("재고 부족");
        }

        @Test
        @DisplayName("SHIPPED 상태 주문 취소 시 예외")
        void cancel_fromShipped_throwsException() {
            // Given
            testOrder.confirm();
            testOrder.ship();

            // When & Then
            assertThatThrownBy(() -> testOrder.cancel("취소 요청"))
                    .isInstanceOf(OrderException.OrderCancellationNotAllowedException.class);
        }

        @Test
        @DisplayName("DELIVERED 상태 주문 취소 시 예외")
        void cancel_fromDelivered_throwsException() {
            // Given
            testOrder.confirm();
            testOrder.ship();
            testOrder.deliver();

            // When & Then
            assertThatThrownBy(() -> testOrder.cancel("취소 요청"))
                    .isInstanceOf(OrderException.OrderCancellationNotAllowedException.class);
        }

        @Test
        @DisplayName("취소 가능 여부 확인")
        void isCancellable_checkStatus() {
            // PENDING
            assertThat(testOrder.isCancellable()).isTrue();

            // CONFIRMED
            testOrder.confirm();
            assertThat(testOrder.isCancellable()).isTrue();

            // SHIPPED
            testOrder.ship();
            assertThat(testOrder.isCancellable()).isFalse();
        }
    }

    @Nested
    @DisplayName("금액 계산")
    class AmountCalculationTest {

        @Test
        @DisplayName("할인 없는 최종 금액")
        void getFinalAmount_noDiscount() {
            // Given
            Order order = Order.builder()
                    .member(testMember)
                    .totalAmount(Money.of(new BigDecimal("50000")))
                    .discountAmount(Money.zero())
                    .orderDate(LocalDateTime.now())
                    .build();

            // When
            Money finalAmount = order.getFinalAmount();

            // Then
            assertThat(finalAmount.getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        }

        @Test
        @DisplayName("할인 적용된 최종 금액")
        void getFinalAmount_withDiscount() {
            // Given
            Order order = Order.builder()
                    .member(testMember)
                    .totalAmount(Money.of(new BigDecimal("50000")))
                    .discountAmount(Money.of(new BigDecimal("10000")))
                    .orderDate(LocalDateTime.now())
                    .build();

            // When
            Money finalAmount = order.getFinalAmount();

            // Then
            assertThat(finalAmount.getAmount()).isEqualByComparingTo(new BigDecimal("40000"));
        }
    }

    @Nested
    @DisplayName("주문 항목 관리")
    class OrderItemManagementTest {

        @Test
        @DisplayName("주문 항목 추가")
        void addOrderItem_success() {
            // Given
            Book book = Book.builder()
                    .id(1L)
                    .title("테스트 책")
                    .build();

            OrderItem item = OrderItem.builder()
                    .book(book)
                    .quantity(2)
                    .price(Money.of(new BigDecimal("15000")))
                    .build();

            // When
            testOrder.addOrderItem(item);

            // Then
            assertThat(testOrder.getOrderItems()).hasSize(1);
            assertThat(item.getOrder()).isEqualTo(testOrder);
        }

        @Test
        @DisplayName("주문에 포함된 도서 목록 조회")
        void getBooks_success() {
            // Given
            Book book1 = Book.builder().id(1L).title("책1").build();
            Book book2 = Book.builder().id(2L).title("책2").build();

            OrderItem item1 = OrderItem.builder().book(book1).quantity(1).price(Money.of(10000)).build();
            OrderItem item2 = OrderItem.builder().book(book2).quantity(2).price(Money.of(20000)).build();

            testOrder.addOrderItem(item1);
            testOrder.addOrderItem(item2);

            // When
            var books = testOrder.getBooks();

            // Then
            assertThat(books).hasSize(2);
            assertThat(books).extracting(Book::getTitle).containsExactly("책1", "책2");
        }
    }
}
