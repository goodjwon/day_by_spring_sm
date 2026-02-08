package com.example.spring.repository;

import com.example.spring.domain.vo.Money;
import com.example.spring.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("PaymentRepository 테스트")
public class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    private Member testMember;
    private Order testOrder;
    private static long uniqueCounter = 0;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .name("Test Member")
                .email("payment" + (++uniqueCounter) + "@test.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();
        entityManager.persist(testMember);

        testOrder = Order.builder()
                .member(testMember)
                .totalAmount(Money.of(new BigDecimal("50000")))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .build();
        entityManager.persist(testOrder);
        entityManager.flush();
    }

    private Payment createPayment(Order order, PaymentStatus status, String transactionId) {
        Payment payment = Payment.builder()
                .order(order)
                .method(PaymentMethod.CREDIT_CARD)
                .status(status)
                .amount(Money.of(new BigDecimal("50000")))
                .transactionId(transactionId)
                .build();
        return entityManager.persistAndFlush(payment);
    }

    // ========== 기본 CRUD 테스트 ==========

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class BasicCrudTest {

        @Test
        @DisplayName("결제 저장 성공")
        void save_결제저장_성공() {
            // Given
            Payment payment = Payment.builder()
                    .order(testOrder)
                    .method(PaymentMethod.CREDIT_CARD)
                    .status(PaymentStatus.PENDING)
                    .amount(Money.of(new BigDecimal("50000")))
                    .build();

            // When
            Payment savedPayment = paymentRepository.save(payment);

            // Then
            assertThat(savedPayment.getId()).isNotNull();
            assertThat(savedPayment.getAmount()).isEqualToComparingFieldByField(new BigDecimal("50000"));
            assertThat(savedPayment.getMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        }

        @Test
        @DisplayName("결제 ID로 조회 성공")
        void findById_존재하는결제_조회성공() {
            // Given
            Payment payment = createPayment(testOrder, PaymentStatus.COMPLETED, "TXN001");

            // When
            Optional<Payment> found = paymentRepository.findById(payment.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getTransactionId()).isEqualTo("TXN001");
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 빈 Optional")
        void findById_존재하지않는결제_빈Optional() {
            // When
            Optional<Payment> found = paymentRepository.findById(999L);

            // Then
            assertThat(found).isEmpty();
        }
    }

    // ========== 주문 ID로 조회 테스트 ==========

    @Nested
    @DisplayName("findByOrderId 테스트")
    class FindByOrderIdTest {

        @Test
        @DisplayName("주문 ID로 결제 조회 성공")
        void findByOrderId_존재하는주문_결제반환() {
            // Given
            createPayment(testOrder, PaymentStatus.COMPLETED, "TXN002");

            // When
            Optional<Payment> found = paymentRepository.findByOrderId(testOrder.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getOrder().getId()).isEqualTo(testOrder.getId());
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 조회 시 빈 Optional")
        void findByOrderId_존재하지않는주문_빈Optional() {
            // When
            Optional<Payment> found = paymentRepository.findByOrderId(999L);

            // Then
            assertThat(found).isEmpty();
        }
    }

    // ========== 거래 ID로 조회 테스트 ==========

    @Nested
    @DisplayName("findByTransactionId 테스트")
    class FindByTransactionIdTest {

        @Test
        @DisplayName("거래 ID로 결제 조회 성공")
        void findByTransactionId_존재하는거래_결제반환() {
            // Given
            String transactionId = "TXN-UNIQUE-123";
            createPayment(testOrder, PaymentStatus.COMPLETED, transactionId);

            // When
            Optional<Payment> found = paymentRepository.findByTransactionId(transactionId);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getTransactionId()).isEqualTo(transactionId);
        }

        @Test
        @DisplayName("존재하지 않는 거래 ID로 조회 시 빈 Optional")
        void findByTransactionId_존재하지않는거래_빈Optional() {
            // When
            Optional<Payment> found = paymentRepository.findByTransactionId("NON-EXISTENT");

            // Then
            assertThat(found).isEmpty();
        }
    }

    // ========== 상태별 조회 테스트 ==========

    @Nested
    @DisplayName("findByStatus 테스트")
    class FindByStatusTest {

        @Test
        @DisplayName("상태별 결제 조회")
        void findByStatus_상태별결제_조회성공() {
            // Given
            createPayment(testOrder, PaymentStatus.COMPLETED, "TXN-C1");

            Order order2 = createNewOrder();
            createPayment(order2, PaymentStatus.COMPLETED, "TXN-C2");

            Order order3 = createNewOrder();
            createPayment(order3, PaymentStatus.PENDING, "TXN-P1");

            // When
            List<Payment> completedPayments = paymentRepository.findByStatus(PaymentStatus.COMPLETED);
            List<Payment> pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING);

            // Then
            assertThat(completedPayments).hasSize(2);
            assertThat(pendingPayments).hasSize(1);
        }

        @Test
        @DisplayName("해당 상태 결제 없으면 빈 리스트")
        void findByStatus_없는상태_빈리스트() {
            // Given
            createPayment(testOrder, PaymentStatus.COMPLETED, "TXN-ONLY");

            // When
            List<Payment> failedPayments = paymentRepository.findByStatus(PaymentStatus.FAILED);

            // Then
            assertThat(failedPayments).isEmpty();
        }
    }

    // ========== 주문 ID와 상태로 조회 테스트 ==========

    @Nested
    @DisplayName("findByOrderIdAndStatus 테스트")
    class FindByOrderIdAndStatusTest {

        @Test
        @DisplayName("주문 ID와 상태로 결제 조회 성공")
        void findByOrderIdAndStatus_일치하는결제_반환() {
            // Given
            createPayment(testOrder, PaymentStatus.COMPLETED, "TXN-MATCH");

            // When
            Optional<Payment> found = paymentRepository.findByOrderIdAndStatus(
                    testOrder.getId(), PaymentStatus.COMPLETED);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("상태 불일치 시 빈 Optional")
        void findByOrderIdAndStatus_상태불일치_빈Optional() {
            // Given
            createPayment(testOrder, PaymentStatus.COMPLETED, "TXN-MISMATCH");

            // When
            Optional<Payment> found = paymentRepository.findByOrderIdAndStatus(
                    testOrder.getId(), PaymentStatus.FAILED);

            // Then
            assertThat(found).isEmpty();
        }
    }

    // ========== 상태 목록으로 조회 테스트 ==========

    @Nested
    @DisplayName("findByStatusIn 테스트")
    class FindByStatusInTest {

        @Test
        @DisplayName("환불 가능한 결제 조회 (COMPLETED, PARTIAL_REFUNDED)")
        void findByStatusIn_환불가능결제_조회성공() {
            // Given
            createPayment(testOrder, PaymentStatus.COMPLETED, "TXN-REF1");

            Order order2 = createNewOrder();
            createPayment(order2, PaymentStatus.PARTIAL_REFUNDED, "TXN-REF2");

            Order order3 = createNewOrder();
            createPayment(order3, PaymentStatus.PENDING, "TXN-PEND");

            Order order4 = createNewOrder();
            createPayment(order4, PaymentStatus.FAILED, "TXN-FAIL");

            // When
            List<Payment> refundablePayments = paymentRepository.findByStatusIn(
                    Arrays.asList(PaymentStatus.COMPLETED, PaymentStatus.PARTIAL_REFUNDED));

            // Then
            assertThat(refundablePayments).hasSize(2);
            assertThat(refundablePayments).allMatch(p ->
                    p.getStatus() == PaymentStatus.COMPLETED ||
                            p.getStatus() == PaymentStatus.PARTIAL_REFUNDED);
        }

        @Test
        @DisplayName("빈 상태 목록으로 조회 시 빈 리스트")
        void findByStatusIn_빈목록_빈리스트() {
            // Given
            createPayment(testOrder, PaymentStatus.COMPLETED, "TXN-EMPTY");

            // When
            List<Payment> result = paymentRepository.findByStatusIn(List.of());

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ========== 삭제 테스트 ==========

    @Nested
    @DisplayName("delete 테스트")
    class DeleteTest {

        @Test
        @DisplayName("결제 삭제 성공")
        void delete_결제삭제_성공() {
            // Given
            Payment payment = createPayment(testOrder, PaymentStatus.CANCELLED, "TXN-DEL");
            Long paymentId = payment.getId();

            // When
            paymentRepository.deleteById(paymentId);
            entityManager.flush();

            // Then
            assertThat(entityManager.find(Payment.class, paymentId)).isNull();
        }
    }

    // ========== Helper Methods ==========

    private Order createNewOrder() {
        Order order = Order.builder()
                .member(testMember)
                .totalAmount(Money.of(new BigDecimal("30000")))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .build();
        return entityManager.persistAndFlush(order);
    }
}
