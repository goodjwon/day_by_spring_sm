package com.example.spring.entity;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderItem 엔티티 테스트")
class OrderItemTest {

    private Book testBook;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn(ISBN.of("9780132350884"))
                .price(Money.of(new BigDecimal("45000")))
                .available(true)
                .build();

        testOrder = Order.builder()
                .id(1L)
                .build();
    }

    @Nested
    @DisplayName("주문 항목 생성")
    class CreateOrderItemTest {

        @Test
        @DisplayName("빌더로 주문 항목 생성")
        void createOrderItem_withBuilder_success() {
            // Given & When
            OrderItem orderItem = OrderItem.builder()
                    .order(testOrder)
                    .book(testBook)
                    .quantity(2)
                    .price(Money.of(new BigDecimal("45000")))
                    .build();

            // Then
            assertThat(orderItem.getOrder()).isEqualTo(testOrder);
            assertThat(orderItem.getBook()).isEqualTo(testBook);
            assertThat(orderItem.getQuantity()).isEqualTo(2);
            assertThat(orderItem.getPrice().getAmount()).isEqualByComparingTo(new BigDecimal("45000"));
        }

        @Test
        @DisplayName("빌더로 주문 항목 생성 - 다른 값")
        void createOrderItem_withBuilder_differentValues() {
            // Given & When
            OrderItem orderItem = OrderItem.builder()
                    .book(testBook)
                    .quantity(3)
                    .price(Money.of(new BigDecimal("30000")))
                    .build();

            // Then
            assertThat(orderItem.getBook()).isEqualTo(testBook);
            assertThat(orderItem.getQuantity()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("총 가격 계산")
    class TotalPriceCalculationTest {

        @Test
        @DisplayName("수량 1개 - 총 가격 = 단가")
        void getTotalPrice_quantity1_equalsPrice() {
            // Given
            OrderItem orderItem = OrderItem.builder()
                    .book(testBook)
                    .quantity(1)
                    .price(Money.of(new BigDecimal("45000")))
                    .build();

            // When
            Money totalPrice = orderItem.getTotalPrice();

            // Then
            assertThat(totalPrice.getAmount()).isEqualByComparingTo(new BigDecimal("45000"));
        }

        @Test
        @DisplayName("수량 3개 - 총 가격 = 단가 * 3")
        void getTotalPrice_quantity3_multiplied() {
            // Given
            OrderItem orderItem = OrderItem.builder()
                    .book(testBook)
                    .quantity(3)
                    .price(Money.of(new BigDecimal("15000")))
                    .build();

            // When
            Money totalPrice = orderItem.getTotalPrice();

            // Then
            assertThat(totalPrice.getAmount()).isEqualByComparingTo(new BigDecimal("45000"));
        }

        @Test
        @DisplayName("수량 10개 - 대량 주문")
        void getTotalPrice_quantity10_bulkOrder() {
            // Given
            OrderItem orderItem = OrderItem.builder()
                    .book(testBook)
                    .quantity(10)
                    .price(Money.of(new BigDecimal("25000")))
                    .build();

            // When
            Money totalPrice = orderItem.getTotalPrice();

            // Then
            assertThat(totalPrice.getAmount()).isEqualByComparingTo(new BigDecimal("250000"));
        }

        @Test
        @DisplayName("소수점 가격 - 정확한 계산")
        void getTotalPrice_decimalPrice_accurate() {
            // Given
            OrderItem orderItem = OrderItem.builder()
                    .book(testBook)
                    .quantity(4)
                    .price(Money.of(new BigDecimal("12500.50")))
                    .build();

            // When
            Money totalPrice = orderItem.getTotalPrice();

            // Then
            assertThat(totalPrice.getAmount()).isEqualByComparingTo(new BigDecimal("50002.00"));
        }
    }

    @Nested
    @DisplayName("주문 항목 수정")
    class UpdateOrderItemTest {

        @Test
        @DisplayName("수량 변경")
        void updateQuantity_success() {
            // Given
            OrderItem orderItem = OrderItem.builder()
                    .book(testBook)
                    .quantity(2)
                    .price(Money.of(new BigDecimal("45000")))
                    .build();

            // When
            orderItem.changeQuantity(5);

            // Then
            assertThat(orderItem.getQuantity()).isEqualTo(5);
            assertThat(orderItem.getTotalPrice().getAmount()).isEqualByComparingTo(new BigDecimal("225000"));
        }

        @Test
        @DisplayName("가격 변경")
        void updatePrice_success() {
            // Given
            OrderItem orderItem = OrderItem.builder()
                    .book(testBook)
                    .quantity(2)
                    .price(Money.of(new BigDecimal("45000")))
                    .build();

            // When
            orderItem.updatePrice(Money.of(new BigDecimal("40000")));

            // Then
            assertThat(orderItem.getPrice().getAmount()).isEqualByComparingTo(new BigDecimal("40000"));
            assertThat(orderItem.getTotalPrice().getAmount()).isEqualByComparingTo(new BigDecimal("80000"));
        }
    }

    @Nested
    @DisplayName("주문과의 관계")
    class OrderRelationshipTest {

        @Test
        @DisplayName("주문에 항목 추가 시 주문이 설정됨")
        void attachToOrder_success() {
            // Given
            OrderItem orderItem = OrderItem.builder()
                    .book(testBook)
                    .quantity(1)
                    .price(Money.of(new BigDecimal("45000")))
                    .build();

            // When - Order.addOrderItem()을 통해 연결
            testOrder.addOrderItem(orderItem);

            // Then
            assertThat(orderItem.getOrder()).isEqualTo(testOrder);
        }

        @Test
        @DisplayName("주문에 항목 추가 시 양방향 관계 설정")
        void addToOrder_bidirectionalRelation() {
            // Given
            OrderItem orderItem = OrderItem.builder()
                    .book(testBook)
                    .quantity(1)
                    .price(Money.of(new BigDecimal("45000")))
                    .build();

            // When
            testOrder.addOrderItem(orderItem);

            // Then
            assertThat(orderItem.getOrder()).isEqualTo(testOrder);
            assertThat(testOrder.getOrderItems()).contains(orderItem);
        }
    }

    @Nested
    @DisplayName("도서와의 관계")
    class BookRelationshipTest {

        @Test
        @DisplayName("도서 정보 접근")
        void accessBookInfo_success() {
            // Given
            OrderItem orderItem = OrderItem.builder()
                    .book(testBook)
                    .quantity(2)
                    .price(Money.of(new BigDecimal("45000")))
                    .build();

            // When
            Book book = orderItem.getBook();

            // Then
            assertThat(book.getTitle()).isEqualTo("Clean Code");
            assertThat(book.getAuthor()).isEqualTo("Robert C. Martin");
        }

        @Test
        @DisplayName("다른 도서로 주문 항목 생성")
        void createWithDifferentBook_success() {
            // Given
            Book newBook = Book.builder()
                    .id(2L)
                    .title("Effective Java")
                    .author("Joshua Bloch")
                    .build();

            // When - 빌더를 통해 다른 도서로 주문 항목 생성
            OrderItem orderItem = OrderItem.builder()
                    .book(newBook)
                    .quantity(1)
                    .price(Money.of(new BigDecimal("45000")))
                    .build();

            // Then
            assertThat(orderItem.getBook().getTitle()).isEqualTo("Effective Java");
        }
    }
}
