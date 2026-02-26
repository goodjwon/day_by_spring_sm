package com.example.spring.application.service;

import com.example.spring.domain.vo.Money;
import com.example.spring.application.dto.response.PaymentResponse;
import com.example.spring.domain.model.Order;
import com.example.spring.domain.model.Payment;
import com.example.spring.domain.model.PaymentMethod;
import com.example.spring.domain.model.PaymentStatus;
import com.example.spring.exception.PaymentException;
import com.example.spring.domain.repository.PaymentRepository;
import com.example.spring.application.service.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceImplTest {

    // 상수 정의
    private static final Long DEFAULT_PAYMENT_ID = 1L;
    private static final Long DEFAULT_ORDER_ID = 1L;
    private static final Long NON_EXISTENT_ID = 999L;
    private static final Money DEFAULT_AMOUNT = Money.of(new BigDecimal("10000"));
    private static final Money PARTIAL_REFUND_AMOUNT = Money.of(new BigDecimal("5000"));
    private static final String TEST_TRANSACTION_ID = "tx-12345";
    private static final String TEST_FAILURE_REASON = "잔액 부족";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPayment = createTestPayment(DEFAULT_PAYMENT_ID, DEFAULT_ORDER_ID, PaymentStatus.PENDING);
    }

    @Nested
    @DisplayName("결제 조회")
    class FindPaymentTest {

        @Test
        @DisplayName("주문 ID로 조회 성공")
        void findByOrderId_성공() {
            // Given
            when(paymentRepository.findByOrderId(DEFAULT_ORDER_ID)).thenReturn(Optional.of(testPayment));

            // When
            PaymentResponse response = paymentService.findByOrderId(DEFAULT_ORDER_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(DEFAULT_PAYMENT_ID);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
            verify(paymentRepository).findByOrderId(DEFAULT_ORDER_ID);
        }

        @Test
        @DisplayName("주문 ID로 조회 실패 - 존재하지 않음")
        void findByOrderId_실패_존재하지않음() {
            // Given
            when(paymentRepository.findByOrderId(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentService.findByOrderId(NON_EXISTENT_ID))
                    .isInstanceOf(PaymentException.PaymentNotFoundException.class)
                    .hasMessageContaining("결제 정보를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("결제 ID로 조회 성공")
        void findById_성공() {
            // Given
            when(paymentRepository.findById(DEFAULT_PAYMENT_ID)).thenReturn(Optional.of(testPayment));

            // When
            PaymentResponse response = paymentService.findById(DEFAULT_PAYMENT_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(DEFAULT_PAYMENT_ID);
            verify(paymentRepository).findById(DEFAULT_PAYMENT_ID);
        }

        @Test
        @DisplayName("결제 ID로 조회 실패 - 존재하지 않음")
        void findById_실패_존재하지않음() {
            // Given
            when(paymentRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentService.findById(NON_EXISTENT_ID))
                    .isInstanceOf(PaymentException.PaymentNotFoundException.class)
                    .hasMessageContaining("결제 정보를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("상태별 조회 성공")
        void findByStatus_성공() {
            // Given
            PaymentStatus status = PaymentStatus.PENDING;
            when(paymentRepository.findByStatus(status)).thenReturn(List.of(testPayment));

            // When
            List<PaymentResponse> responses = paymentService.findByStatus(status);

            // Then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getStatus()).isEqualTo(status);
            verify(paymentRepository).findByStatus(status);
        }

        @Test
        @DisplayName("상태별 조회 - 결과 없음")
        void findByStatus_결과없음() {
            // Given
            PaymentStatus status = PaymentStatus.REFUNDED;
            when(paymentRepository.findByStatus(status)).thenReturn(Collections.emptyList());

            // When
            List<PaymentResponse> responses = paymentService.findByStatus(status);

            // Then
            assertThat(responses).isEmpty();
            verify(paymentRepository).findByStatus(status);
        }
    }

    @Nested
    @DisplayName("결제 완료 처리")
    class CompletePaymentTest {

        @Test
        @DisplayName("정상 결제 완료")
        void completePayment_성공() {
            // Given
            when(paymentRepository.findById(DEFAULT_PAYMENT_ID)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentResponse response = paymentService.completePayment(DEFAULT_PAYMENT_ID, TEST_TRANSACTION_ID);

            // Then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(response.getTransactionId()).isEqualTo(TEST_TRANSACTION_ID);
            verify(paymentRepository).save(testPayment);
        }

        @Test
        @DisplayName("결제 완료 실패 - 이미 완료된 결제")
        void completePayment_실패_이미완료된결제() {
            // Given
            Payment completedPayment = createTestPayment(DEFAULT_PAYMENT_ID, DEFAULT_ORDER_ID, PaymentStatus.COMPLETED);
            when(paymentRepository.findById(DEFAULT_PAYMENT_ID)).thenReturn(Optional.of(completedPayment));

            // When & Then
            assertThatThrownBy(() -> paymentService.completePayment(DEFAULT_PAYMENT_ID, TEST_TRANSACTION_ID))
                    .isInstanceOf(PaymentException.InvalidPaymentStateException.class)
                    .hasMessageContaining("대기 중인 결제만 완료 처리할 수 있습니다");
        }

        @Test
        @DisplayName("결제 완료 실패 - 존재하지 않는 결제")
        void completePayment_실패_존재하지않는결제() {
            // Given
            when(paymentRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentService.completePayment(NON_EXISTENT_ID, TEST_TRANSACTION_ID))
                    .isInstanceOf(PaymentException.PaymentNotFoundException.class)
                    .hasMessageContaining("결제 정보를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("결제 실패 처리")
    class FailPaymentTest {

        @Test
        @DisplayName("결제 실패 처리 성공")
        void failPayment_성공() {
            // Given
            when(paymentRepository.findById(DEFAULT_PAYMENT_ID)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentResponse response = paymentService.failPayment(DEFAULT_PAYMENT_ID, TEST_FAILURE_REASON);

            // Then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(response.getFailureReason()).isEqualTo(TEST_FAILURE_REASON);
            verify(paymentRepository).save(testPayment);
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class CancelPaymentTest {

        @Test
        @DisplayName("결제 취소 성공")
        void cancelPayment_성공() {
            // Given
            Payment completedPayment = createTestPayment(DEFAULT_PAYMENT_ID, DEFAULT_ORDER_ID, PaymentStatus.COMPLETED);
            when(paymentRepository.findById(DEFAULT_PAYMENT_ID)).thenReturn(Optional.of(completedPayment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentResponse response = paymentService.cancelPayment(DEFAULT_PAYMENT_ID);

            // Then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            verify(paymentRepository).save(completedPayment);
        }

        @Test
        @DisplayName("결제 취소 실패 - 완료되지 않은 결제")
        void cancelPayment_실패_완료되지않은결제() {
            // Given
            when(paymentRepository.findById(DEFAULT_PAYMENT_ID)).thenReturn(Optional.of(testPayment));

            // When & Then
            assertThatThrownBy(() -> paymentService.cancelPayment(DEFAULT_PAYMENT_ID))
                    .isInstanceOf(PaymentException.InvalidPaymentStateException.class)
                    .hasMessageContaining("완료된 결제만 취소할 수 있습니다");
        }

        @Test
        @DisplayName("결제 취소 실패 - 존재하지 않는 결제")
        void cancelPayment_실패_존재하지않는결제() {
            // Given
            when(paymentRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentService.cancelPayment(NON_EXISTENT_ID))
                    .isInstanceOf(PaymentException.PaymentNotFoundException.class)
                    .hasMessageContaining("결제 정보를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("결제 환불")
    class RefundPaymentTest {

        @Test
        @DisplayName("전액 환불 성공")
        void refundPayment_성공_전액환불() {
            // Given
            Payment completedPayment = createTestPayment(DEFAULT_PAYMENT_ID, DEFAULT_ORDER_ID, PaymentStatus.COMPLETED, DEFAULT_AMOUNT);
            when(paymentRepository.findById(DEFAULT_PAYMENT_ID)).thenReturn(Optional.of(completedPayment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentResponse response = paymentService.refundPayment(DEFAULT_PAYMENT_ID, DEFAULT_AMOUNT.getAmount());

            // Then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(response.getRefundedAmount()).isEqualByComparingTo(DEFAULT_AMOUNT.getAmount());
            verify(paymentRepository).save(completedPayment);
        }

        @Test
        @DisplayName("부분 환불 성공")
        void refundPayment_성공_부분환불() {
            // Given
            Payment completedPayment = createTestPayment(DEFAULT_PAYMENT_ID, DEFAULT_ORDER_ID, PaymentStatus.COMPLETED, DEFAULT_AMOUNT);
            when(paymentRepository.findById(DEFAULT_PAYMENT_ID)).thenReturn(Optional.of(completedPayment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentResponse response = paymentService.refundPayment(DEFAULT_PAYMENT_ID, PARTIAL_REFUND_AMOUNT.getAmount());

            // Then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.PARTIAL_REFUNDED);
            assertThat(response.getRefundedAmount()).isEqualByComparingTo(PARTIAL_REFUND_AMOUNT.getAmount());
            verify(paymentRepository).save(completedPayment);
        }

        @Test
        @DisplayName("환불 실패 - 완료되지 않은 결제")
        void refundPayment_실패_완료되지않은결제() {
            // Given
            when(paymentRepository.findById(DEFAULT_PAYMENT_ID)).thenReturn(Optional.of(testPayment));

            // When & Then
            assertThatThrownBy(() -> paymentService.refundPayment(DEFAULT_PAYMENT_ID, DEFAULT_AMOUNT.getAmount()))
                    .isInstanceOf(PaymentException.InvalidPaymentStateException.class)
                    .hasMessageContaining("완료된 결제만 환불할 수 있습니다");
        }

        @Test
        @DisplayName("환불 실패 - 환불 금액 초과")
        void refundPayment_실패_환불금액초과() {
            // Given
            Payment completedPayment = createTestPayment(DEFAULT_PAYMENT_ID, DEFAULT_ORDER_ID, PaymentStatus.COMPLETED, DEFAULT_AMOUNT);
            BigDecimal excessAmount = new BigDecimal("20000");
            when(paymentRepository.findById(DEFAULT_PAYMENT_ID)).thenReturn(Optional.of(completedPayment));

            // When & Then
            assertThatThrownBy(() -> paymentService.refundPayment(DEFAULT_PAYMENT_ID, excessAmount))
                    .isInstanceOf(PaymentException.InvalidPaymentAmountException.class)
                    .hasMessageContaining("환불 금액이 결제 금액을 초과할 수 없습니다");
        }
    }

    // --- Helper Methods ---
    private Payment createTestPayment(Long id, Long orderId, PaymentStatus status) {
        return createTestPayment(id, orderId, status, DEFAULT_AMOUNT);
    }

    private Payment createTestPayment(Long id, Long orderId, PaymentStatus status, Money amount) {
        return Payment.builder()
                .id(id)
                .order(Order.builder().id(orderId).build())
                .method(PaymentMethod.CREDIT_CARD)
                .status(status)
                .amount(amount)
                .build();
    }
}