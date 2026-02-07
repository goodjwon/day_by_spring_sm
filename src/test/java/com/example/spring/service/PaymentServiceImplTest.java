package com.example.spring.service;

import com.example.spring.domain.vo.Money;
import com.example.spring.dto.response.PaymentResponse;
import com.example.spring.entity.Order;
import com.example.spring.entity.Payment;
import com.example.spring.entity.PaymentMethod;
import com.example.spring.entity.PaymentStatus;
import com.example.spring.exception.PaymentException;
import com.example.spring.repository.PaymentRepository;
import com.example.spring.service.impl.PaymentServiceImpl;
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

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void findByOrderId_성공() {
        // Given
        Long orderId = 1L;
        Payment payment = createTestPayment(1L, orderId, PaymentStatus.PENDING);
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        // When
        PaymentResponse response = paymentService.findByOrderId(orderId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    void findByOrderId_실패_존재하지않음() {
        // Given
        Long orderId = 999L;
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.findByOrderId(orderId))
                .isInstanceOf(PaymentException.PaymentNotFoundException.class)
                .hasMessageContaining("결제 정보를 찾을 수 없습니다");
    }

    @Test
    void findById_성공() {
        // Given
        Long paymentId = 1L;
        Payment payment = createTestPayment(paymentId, 1L, PaymentStatus.PENDING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When
        PaymentResponse response = paymentService.findById(paymentId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(paymentId);
        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void findByStatus_성공() {
        // Given
        PaymentStatus status = PaymentStatus.PENDING;
        Payment payment = createTestPayment(1L, 1L, status);
        when(paymentRepository.findByStatus(status)).thenReturn(List.of(payment));

        // When
        List<PaymentResponse> responses = paymentService.findByStatus(status);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(status);
        verify(paymentRepository).findByStatus(status);
    }

    @Test
    void completePayment_성공() {
        // Given
        Long paymentId = 1L;
        String transactionId = "tx-12345";
        Payment payment = createTestPayment(paymentId, 1L, PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse response = paymentService.completePayment(paymentId, transactionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.getTransactionId()).isEqualTo(transactionId);
        verify(paymentRepository).save(payment);
    }

    @Test
    void failPayment_성공() {
        // Given
        Long paymentId = 1L;
        String reason = "잔액 부족";
        Payment payment = createTestPayment(paymentId, 1L, PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse response = paymentService.failPayment(paymentId, reason);

        // Then
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.getFailureReason()).isEqualTo(reason);
        verify(paymentRepository).save(payment);
    }

    @Test
    void cancelPayment_성공() {
        // Given
        Long paymentId = 1L;
        Payment payment = createTestPayment(paymentId, 1L, PaymentStatus.COMPLETED); // 취소는 완료된 상태여야 함

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse response = paymentService.cancelPayment(paymentId);

        // Then
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        verify(paymentRepository).save(payment);
    }

    @Test
    void cancelPayment_실패_완료되지않은결제() {
        // Given
        Long paymentId = 1L;
        Payment payment = createTestPayment(paymentId, 1L, PaymentStatus.PENDING); // 완료되지 않음

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> paymentService.cancelPayment(paymentId))
                .isInstanceOf(PaymentException.InvalidPaymentStateException.class)
                .hasMessageContaining("완료된 결제만 취소할 수 있습니다");
    }

    @Test
    void refundPayment_성공_전액환불() {
        // Given
        Long paymentId = 1L;
        BigDecimal amount = new BigDecimal("10000");
        Payment payment = createTestPaymentWithAmount(paymentId, 1L, PaymentStatus.COMPLETED, Money.of(amount));

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse response = paymentService.refundPayment(paymentId, amount);

        // Then
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED); // 전액 환불 시 REFUNDED
        assertThat(response.getRefundedAmount()).isEqualByComparingTo(amount);
        verify(paymentRepository).save(payment);
    }

    @Test
    void refundPayment_성공_부분환불() {
        // Given
        Long paymentId = 1L;
        BigDecimal amount = new BigDecimal("10000");
        BigDecimal refundAmount = new BigDecimal("5000");
        Payment payment = createTestPaymentWithAmount(paymentId, 1L, PaymentStatus.COMPLETED, Money.of(amount));

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse response = paymentService.refundPayment(paymentId, refundAmount);

        // Then
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PARTIAL_REFUNDED); // 부분 환불 시 PARTIAL_REFUNDED
        assertThat(response.getRefundedAmount()).isEqualByComparingTo(refundAmount);
        verify(paymentRepository).save(payment);
    }

    // --- Helper Methods ---
    private Payment createTestPayment(Long id, Long orderId, PaymentStatus status) {
        return Payment.builder()
                .id(id)
                .order(Order.builder().id(orderId).build())
                .method(PaymentMethod.CREDIT_CARD)
                .status(status)
                .amount(Money.of(new BigDecimal("10000")))
                .build();
    }

    private Payment createTestPaymentWithAmount(Long id, Long orderId, PaymentStatus status, Money amount) {
        return Payment.builder()
                .id(id)
                .order(Order.builder().id(orderId).build())
                .method(PaymentMethod.CREDIT_CARD)
                .status(status)
                .amount(amount)
                .build();
    }
}
