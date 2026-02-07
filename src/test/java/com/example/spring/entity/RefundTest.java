package com.example.spring.entity;

import com.example.spring.domain.vo.Money;
import com.example.spring.exception.RefundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Refund 엔티티 테스트")
class RefundTest {

    private Refund testRefund;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id(1L)
                .totalAmount(Money.of(new BigDecimal("50000")))
                .build();

        testRefund = Refund.builder()
                .id(1L)
                .order(testOrder)
                .amount(Money.of(new BigDecimal("50000")))
                .reason("상품 불량")
                .requestedBy("고객")
                .requestDate(LocalDateTime.now())
                .status(RefundStatus.REQUESTED)
                .build();
    }

    @Nested
    @DisplayName("환불 생성")
    class CreateRefundTest {

        @Test
        @DisplayName("환불 요청 생성 - 기본 상태 REQUESTED")
        void createRefund_defaultStatus_requested() {
            // Given & When
            Refund refund = Refund.builder()
                    .order(testOrder)
                    .amount(Money.of(new BigDecimal("30000")))
                    .reason("단순 변심")
                    .requestedBy("홍길동")
                    .requestDate(LocalDateTime.now())
                    .build();

            // Then
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.REQUESTED);
            assertThat(refund.getReason()).isEqualTo("단순 변심");
            assertThat(refund.getRequestedBy()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("환불 계좌 정보 포함하여 생성")
        void createRefund_withBankInfo_success() {
            // Given & When
            Refund refund = Refund.builder()
                    .order(testOrder)
                    .amount(Money.of(new BigDecimal("50000")))
                    .reason("환불 요청")
                    .requestedBy("고객")
                    .requestDate(LocalDateTime.now())
                    .bankName("국민은행")
                    .accountNumber("123-456-789012")
                    .accountHolder("홍길동")
                    .build();

            // Then
            assertThat(refund.getBankName()).isEqualTo("국민은행");
            assertThat(refund.getAccountNumber()).isEqualTo("123-456-789012");
            assertThat(refund.getAccountHolder()).isEqualTo("홍길동");
        }
    }

    @Nested
    @DisplayName("환불 승인")
    class ApproveRefundTest {

        @Test
        @DisplayName("환불 승인 성공")
        void approve_fromRequested_success() {
            // Given
            String approver = "관리자";

            // When
            testRefund.approve(approver);

            // Then
            assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.APPROVED);
            assertThat(testRefund.getApprovedBy()).isEqualTo(approver);
            assertThat(testRefund.getApprovedDate()).isNotNull();
        }

        @Test
        @DisplayName("이미 승인된 환불 다시 승인 시 예외")
        void approve_fromApproved_throwsException() {
            // Given
            testRefund.approve("관리자1");

            // When & Then
            assertThatThrownBy(() -> testRefund.approve("관리자2"))
                    .isInstanceOf(RefundException.InvalidRefundStateException.class)
                    .hasMessageContaining("요청된 환불만 승인");
        }
    }

    @Nested
    @DisplayName("환불 거부")
    class RejectRefundTest {

        @Test
        @DisplayName("환불 거부 성공")
        void reject_fromRequested_success() {
            // Given
            String rejecter = "관리자";
            String rejectionReason = "환불 기간 초과";

            // When
            testRefund.reject(rejecter, rejectionReason);

            // Then
            assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.REJECTED);
            assertThat(testRefund.getRejectedBy()).isEqualTo(rejecter);
            assertThat(testRefund.getRejectionReason()).isEqualTo(rejectionReason);
            assertThat(testRefund.getRejectedDate()).isNotNull();
        }

        @Test
        @DisplayName("승인된 환불 거부 시 예외")
        void reject_fromApproved_throwsException() {
            // Given
            testRefund.approve("관리자");

            // When & Then
            assertThatThrownBy(() -> testRefund.reject("다른관리자", "사유"))
                    .isInstanceOf(RefundException.InvalidRefundStateException.class)
                    .hasMessageContaining("요청된 환불만 거부");
        }
    }

    @Nested
    @DisplayName("환불 처리 시작")
    class StartProcessingTest {

        @Test
        @DisplayName("처리 시작 성공")
        void startProcessing_fromApproved_success() {
            // Given
            testRefund.approve("관리자");

            // When
            testRefund.startProcessing();

            // Then
            assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.PROCESSING);
        }

        @Test
        @DisplayName("미승인 환불 처리 시작 시 예외")
        void startProcessing_fromRequested_throwsException() {
            // When & Then
            assertThatThrownBy(() -> testRefund.startProcessing())
                    .isInstanceOf(RefundException.InvalidRefundStateException.class)
                    .hasMessageContaining("승인된 환불만 처리를 시작");
        }
    }

    @Nested
    @DisplayName("환불 완료")
    class CompleteRefundTest {

        @Test
        @DisplayName("환불 완료 성공")
        void complete_fromProcessing_success() {
            // Given
            testRefund.approve("관리자");
            testRefund.startProcessing();
            String transactionId = "REFUND-TXN-12345";

            // When
            testRefund.complete(transactionId);

            // Then
            assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
            assertThat(testRefund.getRefundTransactionId()).isEqualTo(transactionId);
            assertThat(testRefund.getCompletedDate()).isNotNull();
        }

        @Test
        @DisplayName("처리 중이 아닌 환불 완료 시 예외")
        void complete_fromApproved_throwsException() {
            // Given
            testRefund.approve("관리자");

            // When & Then
            assertThatThrownBy(() -> testRefund.complete("TXN-123"))
                    .isInstanceOf(RefundException.InvalidRefundStateException.class)
                    .hasMessageContaining("처리중인 환불만 완료");
        }

        @Test
        @DisplayName("환불 완료 여부 확인")
        void isCompleted_afterComplete_true() {
            // Given
            testRefund.approve("관리자");
            testRefund.startProcessing();
            testRefund.complete("TXN-123");

            // When & Then
            assertThat(testRefund.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("미완료 환불 확인")
        void isCompleted_beforeComplete_false() {
            assertThat(testRefund.isCompleted()).isFalse();

            testRefund.approve("관리자");
            assertThat(testRefund.isCompleted()).isFalse();

            testRefund.startProcessing();
            assertThat(testRefund.isCompleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("환불 실패")
    class FailRefundTest {

        @Test
        @DisplayName("환불 실패 처리")
        void fail_success() {
            // Given
            String memo = "계좌 정보 오류";

            // When
            testRefund.fail(memo);

            // Then
            assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.FAILED);
            assertThat(testRefund.getProcessingMemo()).isEqualTo(memo);
        }
    }

    @Nested
    @DisplayName("환불 취소 가능 여부")
    class CanCancelTest {

        @Test
        @DisplayName("REQUESTED 상태 - 취소 가능")
        void canCancel_requested_true() {
            assertThat(testRefund.canCancel()).isTrue();
        }

        @Test
        @DisplayName("APPROVED 상태 - 취소 가능")
        void canCancel_approved_true() {
            // Given
            testRefund.approve("관리자");

            // When & Then
            assertThat(testRefund.canCancel()).isTrue();
        }

        @Test
        @DisplayName("PROCESSING 상태 - 취소 불가")
        void canCancel_processing_false() {
            // Given
            testRefund.approve("관리자");
            testRefund.startProcessing();

            // When & Then
            assertThat(testRefund.canCancel()).isFalse();
        }

        @Test
        @DisplayName("COMPLETED 상태 - 취소 불가")
        void canCancel_completed_false() {
            // Given
            testRefund.approve("관리자");
            testRefund.startProcessing();
            testRefund.complete("TXN-123");

            // When & Then
            assertThat(testRefund.canCancel()).isFalse();
        }
    }

    @Nested
    @DisplayName("환불 상태 테스트")
    class RefundStatusTest {

        @Test
        @DisplayName("모든 환불 상태 확인")
        void allRefundStatuses() {
            assertThat(RefundStatus.values()).contains(
                    RefundStatus.REQUESTED,
                    RefundStatus.APPROVED,
                    RefundStatus.REJECTED,
                    RefundStatus.PROCESSING,
                    RefundStatus.COMPLETED,
                    RefundStatus.FAILED
            );
        }
    }

    @Nested
    @DisplayName("환불 상태 전이 시나리오")
    class RefundWorkflowTest {

        @Test
        @DisplayName("정상 환불 흐름: REQUESTED -> APPROVED -> PROCESSING -> COMPLETED")
        void normalRefundFlow() {
            // REQUESTED (초기 상태)
            assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.REQUESTED);

            // APPROVED
            testRefund.approve("관리자");
            assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.APPROVED);

            // PROCESSING
            testRefund.startProcessing();
            assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.PROCESSING);

            // COMPLETED
            testRefund.complete("TXN-12345");
            assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
            assertThat(testRefund.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("환불 거부 흐름: REQUESTED -> REJECTED")
        void rejectedRefundFlow() {
            // REQUESTED (초기 상태)
            assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.REQUESTED);

            // REJECTED
            testRefund.reject("관리자", "환불 불가 사유");
            assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.REJECTED);
            assertThat(testRefund.isCompleted()).isFalse();
        }
    }
}
