package com.example.spring.entity;

import com.example.spring.domain.vo.Money;
import com.example.spring.exception.PaymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Payment 엔티티 테스트")
class PaymentTest {

    private Payment testPayment;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id(1L)
                .totalAmount(Money.of(new BigDecimal("50000")))
                .build();

        testPayment = Payment.builder()
                .id(1L)
                .order(testOrder)
                .method(PaymentMethod.CREDIT_CARD)
                .amount(Money.of(new BigDecimal("50000")))
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("결제 생성")
    class CreatePaymentTest {

        @Test
        @DisplayName("신용카드 결제 생성")
        void createPayment_creditCard_success() {
            // Given & When
            Payment payment = Payment.builder()
                    .order(testOrder)
                    .method(PaymentMethod.CREDIT_CARD)
                    .amount(Money.of(new BigDecimal("30000")))
                    .cardCompany("삼성카드")
                    .cardNumber("1234-****-****-5678")
                    .installmentMonths(3)
                    .build();

            // Then
            assertThat(payment.getMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getCardCompany()).isEqualTo("삼성카드");
            assertThat(payment.getInstallmentMonths()).isEqualTo(3);
        }

        @Test
        @DisplayName("계좌이체 결제 생성")
        void createPayment_bankTransfer_success() {
            // Given & When
            Payment payment = Payment.builder()
                    .order(testOrder)
                    .method(PaymentMethod.BANK_TRANSFER)
                    .amount(Money.of(new BigDecimal("50000")))
                    .pgProvider("토스페이먼츠")
                    .build();

            // Then
            assertThat(payment.getMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
            assertThat(payment.getPgProvider()).isEqualTo("토스페이먼츠");
        }
    }

    @Nested
    @DisplayName("결제 완료")
    class CompletePaymentTest {

        @Test
        @DisplayName("결제 완료 처리 성공")
        void complete_success() {
            // Given
            String transactionId = "TXN-12345678";

            // When
            testPayment.complete(transactionId);

            // Then
            assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(testPayment.getTransactionId()).isEqualTo(transactionId);
            assertThat(testPayment.getPaymentDate()).isNotNull();
        }

        @Test
        @DisplayName("완료된 결제 확인")
        void isCompleted_afterComplete_true() {
            // Given
            testPayment.complete("TXN-12345678");

            // When & Then
            assertThat(testPayment.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("대기 중인 결제는 미완료")
        void isCompleted_pending_false() {
            assertThat(testPayment.isCompleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("결제 실패")
    class FailPaymentTest {

        @Test
        @DisplayName("결제 실패 처리")
        void fail_success() {
            // Given
            String failureReason = "잔액 부족";

            // When
            testPayment.fail(failureReason);

            // Then
            assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(testPayment.getFailureReason()).isEqualTo(failureReason);
            assertThat(testPayment.getFailedDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class CancelPaymentTest {

        @Test
        @DisplayName("완료된 결제 취소 성공")
        void cancel_fromCompleted_success() {
            // Given
            testPayment.complete("TXN-12345678");

            // When
            testPayment.cancel();

            // Then
            assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(testPayment.getCancelledDate()).isNotNull();
        }

        @Test
        @DisplayName("대기 중인 결제 취소 시 예외")
        void cancel_fromPending_throwsException() {
            // When & Then
            assertThatThrownBy(() -> testPayment.cancel())
                    .isInstanceOf(PaymentException.InvalidPaymentStateException.class)
                    .hasMessageContaining("완료된 결제만 취소");
        }

        @Test
        @DisplayName("실패한 결제 취소 시 예외")
        void cancel_fromFailed_throwsException() {
            // Given
            testPayment.fail("오류 발생");

            // When & Then
            assertThatThrownBy(() -> testPayment.cancel())
                    .isInstanceOf(PaymentException.InvalidPaymentStateException.class);
        }
    }

    @Nested
    @DisplayName("환불 처리")
    class RefundPaymentTest {

        @Test
        @DisplayName("전액 환불 성공")
        void refund_fullAmount_success() {
            // Given
            testPayment.complete("TXN-12345678");
            Money refundAmount = Money.of(new BigDecimal("50000"));

            // When
            testPayment.refund(refundAmount);

            // Then
            assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(testPayment.getRefundedAmount().getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
            assertThat(testPayment.getRefundedDate()).isNotNull();
        }

        @Test
        @DisplayName("부분 환불 성공")
        void refund_partialAmount_success() {
            // Given
            testPayment.complete("TXN-12345678");
            Money refundAmount = Money.of(new BigDecimal("20000"));

            // When
            testPayment.refund(refundAmount);

            // Then
            assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PARTIAL_REFUNDED);
            assertThat(testPayment.getRefundedAmount().getAmount()).isEqualByComparingTo(new BigDecimal("20000"));
        }

        @Test
        @DisplayName("대기 중인 결제 환불 시 예외")
        void refund_fromPending_throwsException() {
            // Given
            Money refundAmount = Money.of(new BigDecimal("50000"));

            // When & Then
            assertThatThrownBy(() -> testPayment.refund(refundAmount))
                    .isInstanceOf(PaymentException.InvalidPaymentStateException.class)
                    .hasMessageContaining("완료된 결제만 환불");
        }

        @Test
        @DisplayName("환불 가능 여부 확인 - COMPLETED")
        void isRefundable_completed_true() {
            // Given
            testPayment.complete("TXN-12345678");

            // When & Then
            assertThat(testPayment.isRefundable()).isTrue();
        }

        @Test
        @DisplayName("환불 가능 여부 확인 - PARTIAL_REFUNDED")
        void isRefundable_partialRefunded_true() {
            // Given
            testPayment.complete("TXN-12345678");
            testPayment.refund(Money.of(new BigDecimal("20000")));

            // When & Then
            assertThat(testPayment.isRefundable()).isTrue();
        }

        @Test
        @DisplayName("환불 가능 여부 확인 - PENDING")
        void isRefundable_pending_false() {
            assertThat(testPayment.isRefundable()).isFalse();
        }
    }

    @Nested
    @DisplayName("결제 수단 테스트")
    class PaymentMethodTest {

        @Test
        @DisplayName("모든 결제 수단 확인")
        void allPaymentMethods() {
            assertThat(PaymentMethod.values()).contains(
                    PaymentMethod.CREDIT_CARD,
                    PaymentMethod.DEBIT_CARD,
                    PaymentMethod.BANK_TRANSFER,
                    PaymentMethod.VIRTUAL_ACCOUNT,
                    PaymentMethod.KAKAO_PAY,
                    PaymentMethod.NAVER_PAY,
                    PaymentMethod.TOSS_PAY,
                    PaymentMethod.PAYCO,
                    PaymentMethod.PHONE_BILL
            );
        }
    }

    @Nested
    @DisplayName("결제 상태 테스트")
    class PaymentStatusTest {

        @Test
        @DisplayName("모든 결제 상태 확인")
        void allPaymentStatuses() {
            assertThat(PaymentStatus.values()).contains(
                    PaymentStatus.PENDING,
                    PaymentStatus.COMPLETED,
                    PaymentStatus.FAILED,
                    PaymentStatus.CANCELLED,
                    PaymentStatus.REFUNDED,
                    PaymentStatus.PARTIAL_REFUNDED
            );
        }
    }
}
