package com.example.spring.repository;

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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("RefundRepository 테스트")
public class RefundRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefundRepository refundRepository;

    private Member testMember;
    private Order testOrder;
    private static long uniqueCounter = 0;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .name("Test Member")
                .email("refund" + (++uniqueCounter) + "@test.com")
                .password("password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();
        entityManager.persist(testMember);

        testOrder = Order.builder()
                .member(testMember)
                .totalAmount(new BigDecimal("50000"))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.DELIVERED)
                .build();
        entityManager.persist(testOrder);
        entityManager.flush();
    }

    private Refund createRefund(Order order, RefundStatus status, BigDecimal amount,
                                String requestedBy, String transactionId) {
        Refund refund = Refund.builder()
                .order(order)
                .status(status)
                .amount(amount)
                .reason("테스트 환불 사유")
                .requestedBy(requestedBy)
                .requestDate(LocalDateTime.now())
                .refundTransactionId(transactionId)
                .build();
        return entityManager.persistAndFlush(refund);
    }

    // ========== 기본 CRUD 테스트 ==========

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class BasicCrudTest {

        @Test
        @DisplayName("환불 저장 성공")
        void save_환불저장_성공() {
            // Given
            Refund refund = Refund.builder()
                    .order(testOrder)
                    .status(RefundStatus.REQUESTED)
                    .amount(new BigDecimal("30000"))
                    .reason("상품 불량")
                    .requestedBy("고객")
                    .requestDate(LocalDateTime.now())
                    .build();

            // When
            Refund saved = refundRepository.save(refund);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("30000"));
            assertThat(saved.getReason()).isEqualTo("상품 불량");
        }

        @Test
        @DisplayName("환불 ID로 조회 성공")
        void findById_존재하는환불_조회성공() {
            // Given
            Refund refund = createRefund(testOrder, RefundStatus.REQUESTED,
                    new BigDecimal("20000"), "고객", null);

            // When
            Optional<Refund> found = refundRepository.findById(refund.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("20000"));
        }

        @Test
        @DisplayName("존재하지 않는 환불 조회 시 빈 Optional")
        void findById_존재하지않는환불_빈Optional() {
            // When
            Optional<Refund> found = refundRepository.findById(999L);

            // Then
            assertThat(found).isEmpty();
        }
    }

    // ========== 주문 ID로 조회 테스트 ==========

    @Nested
    @DisplayName("findByOrderId 테스트")
    class FindByOrderIdTest {

        @Test
        @DisplayName("주문 ID로 환불 목록 조회 (부분 환불)")
        void findByOrderId_여러환불_리스트반환() {
            // Given - 부분 환불 시나리오
            createRefund(testOrder, RefundStatus.COMPLETED,
                    new BigDecimal("10000"), "고객", "REF-001");
            createRefund(testOrder, RefundStatus.REQUESTED,
                    new BigDecimal("5000"), "고객", null);

            // When
            List<Refund> refunds = refundRepository.findByOrderId(testOrder.getId());

            // Then
            assertThat(refunds).hasSize(2);
        }

        @Test
        @DisplayName("환불 없는 주문 조회 시 빈 리스트")
        void findByOrderId_환불없음_빈리스트() {
            // When
            List<Refund> refunds = refundRepository.findByOrderId(testOrder.getId());

            // Then
            assertThat(refunds).isEmpty();
        }
    }

    // ========== 상태별 조회 테스트 ==========

    @Nested
    @DisplayName("findByStatus 테스트")
    class FindByStatusTest {

        @Test
        @DisplayName("상태별 환불 조회")
        void findByStatus_상태별환불_조회성공() {
            // Given
            createRefund(testOrder, RefundStatus.REQUESTED,
                    new BigDecimal("10000"), "고객1", null);

            Order order2 = createNewOrder();
            createRefund(order2, RefundStatus.REQUESTED,
                    new BigDecimal("20000"), "고객2", null);

            Order order3 = createNewOrder();
            createRefund(order3, RefundStatus.COMPLETED,
                    new BigDecimal("15000"), "고객3", "REF-003");

            // When
            List<Refund> requestedRefunds = refundRepository.findByStatus(RefundStatus.REQUESTED);
            List<Refund> completedRefunds = refundRepository.findByStatus(RefundStatus.COMPLETED);

            // Then
            assertThat(requestedRefunds).hasSize(2);
            assertThat(completedRefunds).hasSize(1);
        }

        @Test
        @DisplayName("해당 상태 환불 없으면 빈 리스트")
        void findByStatus_없는상태_빈리스트() {
            // Given
            createRefund(testOrder, RefundStatus.REQUESTED,
                    new BigDecimal("10000"), "고객", null);

            // When
            List<Refund> rejectedRefunds = refundRepository.findByStatus(RefundStatus.REJECTED);

            // Then
            assertThat(rejectedRefunds).isEmpty();
        }
    }

    // ========== 요청자로 조회 테스트 ==========

    @Nested
    @DisplayName("findByRequestedBy 테스트")
    class FindByRequestedByTest {

        @Test
        @DisplayName("요청자별 환불 조회")
        void findByRequestedBy_요청자별_조회성공() {
            // Given
            createRefund(testOrder, RefundStatus.REQUESTED,
                    new BigDecimal("10000"), "고객A", null);

            Order order2 = createNewOrder();
            createRefund(order2, RefundStatus.REQUESTED,
                    new BigDecimal("20000"), "관리자", null);

            Order order3 = createNewOrder();
            createRefund(order3, RefundStatus.COMPLETED,
                    new BigDecimal("15000"), "고객A", "REF-003");

            // When
            List<Refund> customerRefunds = refundRepository.findByRequestedBy("고객A");
            List<Refund> adminRefunds = refundRepository.findByRequestedBy("관리자");

            // Then
            assertThat(customerRefunds).hasSize(2);
            assertThat(adminRefunds).hasSize(1);
        }
    }

    // ========== 날짜 범위로 조회 테스트 ==========

    @Nested
    @DisplayName("findByRequestDateBetween 테스트")
    class FindByRequestDateBetweenTest {

        @Test
        @DisplayName("날짜 범위 내 환불 조회")
        void findByRequestDateBetween_범위내환불_조회성공() {
            // Given
            LocalDateTime now = LocalDateTime.now();

            // @PrePersist가 requestDate를 덮어쓰므로, persist 후에 날짜를 수정
            Refund refund1 = Refund.builder()
                    .order(testOrder)
                    .status(RefundStatus.REQUESTED)
                    .amount(new BigDecimal("10000"))
                    .reason("사유1")
                    .requestedBy("고객")
                    .build();
            entityManager.persist(refund1);
            refund1.setRequestDate(now.minusDays(5));

            Order order2 = createNewOrder();
            Refund refund2 = Refund.builder()
                    .order(order2)
                    .status(RefundStatus.REQUESTED)
                    .amount(new BigDecimal("20000"))
                    .reason("사유2")
                    .requestedBy("고객")
                    .build();
            entityManager.persist(refund2);
            refund2.setRequestDate(now.minusDays(10));

            entityManager.flush();

            // When
            List<Refund> refunds = refundRepository.findByRequestDateBetween(
                    now.minusDays(7), now);

            // Then - 5일 전 환불만 조회됨
            assertThat(refunds).hasSize(1);
            assertThat(refunds.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("10000"));
        }
    }

    // ========== 주문 ID와 상태로 조회 테스트 ==========

    @Nested
    @DisplayName("findByOrderIdAndStatus 테스트")
    class FindByOrderIdAndStatusTest {

        @Test
        @DisplayName("주문 ID와 상태로 환불 조회")
        void findByOrderIdAndStatus_일치_조회성공() {
            // Given
            createRefund(testOrder, RefundStatus.REQUESTED,
                    new BigDecimal("10000"), "고객", null);
            createRefund(testOrder, RefundStatus.COMPLETED,
                    new BigDecimal("5000"), "고객", "REF-001");

            // When
            List<Refund> requestedRefunds = refundRepository.findByOrderIdAndStatus(
                    testOrder.getId(), RefundStatus.REQUESTED);
            List<Refund> completedRefunds = refundRepository.findByOrderIdAndStatus(
                    testOrder.getId(), RefundStatus.COMPLETED);

            // Then
            assertThat(requestedRefunds).hasSize(1);
            assertThat(completedRefunds).hasSize(1);
        }
    }

    // ========== 대기 중인 환불 조회 테스트 ==========

    @Nested
    @DisplayName("findPendingRefunds 테스트")
    class FindPendingRefundsTest {

        @Test
        @DisplayName("승인 대기 중인 환불 조회 (REQUESTED 상태)")
        void findPendingRefunds_대기중환불_조회성공() {
            // Given
            createRefund(testOrder, RefundStatus.REQUESTED,
                    new BigDecimal("10000"), "고객1", null);

            Order order2 = createNewOrder();
            createRefund(order2, RefundStatus.REQUESTED,
                    new BigDecimal("20000"), "고객2", null);

            Order order3 = createNewOrder();
            createRefund(order3, RefundStatus.APPROVED,
                    new BigDecimal("15000"), "고객3", null);

            // When
            List<Refund> pendingRefunds = refundRepository.findPendingRefunds();

            // Then
            assertThat(pendingRefunds).hasSize(2);
            assertThat(pendingRefunds).allMatch(r -> r.getStatus() == RefundStatus.REQUESTED);
        }

        @Test
        @DisplayName("대기 중인 환불 없으면 빈 리스트")
        void findPendingRefunds_없음_빈리스트() {
            // Given
            createRefund(testOrder, RefundStatus.COMPLETED,
                    new BigDecimal("10000"), "고객", "REF-001");

            // When
            List<Refund> pendingRefunds = refundRepository.findPendingRefunds();

            // Then
            assertThat(pendingRefunds).isEmpty();
        }
    }

    // ========== 거래 ID로 조회 테스트 ==========

    @Nested
    @DisplayName("findByRefundTransactionId 테스트")
    class FindByRefundTransactionIdTest {

        @Test
        @DisplayName("환불 거래 ID로 조회 성공")
        void findByRefundTransactionId_존재하는거래_환불반환() {
            // Given
            String transactionId = "REF-TXN-12345";
            createRefund(testOrder, RefundStatus.COMPLETED,
                    new BigDecimal("30000"), "고객", transactionId);

            // When
            Optional<Refund> found = refundRepository.findByRefundTransactionId(transactionId);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getRefundTransactionId()).isEqualTo(transactionId);
        }

        @Test
        @DisplayName("존재하지 않는 거래 ID로 조회 시 빈 Optional")
        void findByRefundTransactionId_없음_빈Optional() {
            // When
            Optional<Refund> found = refundRepository.findByRefundTransactionId("NON-EXISTENT");

            // Then
            assertThat(found).isEmpty();
        }
    }

    // ========== 총 환불 금액 계산 테스트 ==========

    @Nested
    @DisplayName("calculateTotalRefundedAmount 테스트")
    class CalculateTotalRefundedAmountTest {

        @Test
        @DisplayName("주문의 총 환불 금액 계산 (완료된 환불만)")
        void calculateTotalRefundedAmount_완료된환불합계() {
            // Given
            createRefund(testOrder, RefundStatus.COMPLETED,
                    new BigDecimal("10000"), "고객", "REF-001");
            createRefund(testOrder, RefundStatus.COMPLETED,
                    new BigDecimal("5000"), "고객", "REF-002");
            createRefund(testOrder, RefundStatus.REQUESTED,  // 요청 중인 것은 제외
                    new BigDecimal("3000"), "고객", null);

            // When
            BigDecimal totalRefunded = refundRepository.calculateTotalRefundedAmount(testOrder.getId());

            // Then - COMPLETED 상태인 10000 + 5000 = 15000
            assertThat(totalRefunded).isEqualByComparingTo(new BigDecimal("15000"));
        }

        @Test
        @DisplayName("환불 없는 주문의 총 환불 금액은 0")
        void calculateTotalRefundedAmount_환불없음_0() {
            // When
            BigDecimal totalRefunded = refundRepository.calculateTotalRefundedAmount(testOrder.getId());

            // Then
            assertThat(totalRefunded).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("완료된 환불 없으면 0")
        void calculateTotalRefundedAmount_완료된환불없음_0() {
            // Given
            createRefund(testOrder, RefundStatus.REQUESTED,
                    new BigDecimal("10000"), "고객", null);

            // When
            BigDecimal totalRefunded = refundRepository.calculateTotalRefundedAmount(testOrder.getId());

            // Then
            assertThat(totalRefunded).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ========== 삭제 테스트 ==========

    @Nested
    @DisplayName("delete 테스트")
    class DeleteTest {

        @Test
        @DisplayName("환불 삭제 성공")
        void delete_환불삭제_성공() {
            // Given
            Refund refund = createRefund(testOrder, RefundStatus.REJECTED,
                    new BigDecimal("10000"), "고객", null);
            Long refundId = refund.getId();

            // When
            refundRepository.deleteById(refundId);
            entityManager.flush();

            // Then
            assertThat(entityManager.find(Refund.class, refundId)).isNull();
        }
    }

    // ========== Helper Methods ==========

    private Order createNewOrder() {
        Order order = Order.builder()
                .member(testMember)
                .totalAmount(new BigDecimal("30000"))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.DELIVERED)
                .build();
        return entityManager.persistAndFlush(order);
    }
}