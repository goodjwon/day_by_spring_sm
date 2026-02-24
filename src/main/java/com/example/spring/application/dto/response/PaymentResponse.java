package com.example.spring.application.dto.response;

import com.example.spring.domain.model.Payment;
import com.example.spring.domain.model.PaymentMethod;
import com.example.spring.domain.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 정보 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String transactionId;
    private String pgProvider;

    // 카드 정보 (선택적)
    private String cardCompany;
    private String cardNumber;  // 마스킹된 카드번호
    private Integer installmentMonths;

    // 실패 정보
    private String failureReason;
    private LocalDateTime failedDate;

    // 취소/환불 정보
    private LocalDateTime cancelledDate;
    private BigDecimal refundedAmount;
    private LocalDateTime refundedDate;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /**
     * Payment 엔티티를 PaymentResponse로 변환
     */
    public static PaymentResponse from(Payment payment) {
        if (payment == null) {
            return null;
        }
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .amount(payment.getAmount() != null ? payment.getAmount().getAmount() : null)
                .paymentDate(payment.getPaymentDate())
                .transactionId(payment.getTransactionId())
                .pgProvider(payment.getPgProvider())
                .cardCompany(payment.getCardCompany())
                .cardNumber(payment.getCardNumber())
                .installmentMonths(payment.getInstallmentMonths())
                .failureReason(payment.getFailureReason())
                .failedDate(payment.getFailedDate())
                .cancelledDate(payment.getCancelledDate())
                .refundedAmount(payment.getRefundedAmount() != null ? payment.getRefundedAmount().getAmount() : null)
                .refundedDate(payment.getRefundedDate())
                .createdDate(payment.getCreatedDate())
                .updatedDate(payment.getUpdatedDate())
                .build();
    }
}