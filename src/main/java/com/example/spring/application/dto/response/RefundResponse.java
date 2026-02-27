package com.example.spring.application.dto.response;

import com.example.spring.domain.model.Refund;
import com.example.spring.domain.model.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환불 정보 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {
    private Long id;
    private Long orderId;
    private RefundStatus status;
    private BigDecimal amount;
    private String reason;
    private String requestedBy;

    // 날짜 정보
    private LocalDateTime requestDate;
    private LocalDateTime approvedDate;
    private LocalDateTime rejectedDate;
    private LocalDateTime completedDate;

    // 승인/거부 정보
    private String approvedBy;
    private String rejectedBy;
    private String rejectionReason;

    // 환불 계좌 정보
    private String bankName;
    private String accountNumber;
    private String accountHolder;

    // 환불 처리 정보
    private String refundTransactionId;
    private String processingMemo;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /**
     * Refund 엔티티를 RefundResponse로 변환
     */
    public static RefundResponse from(Refund refund) {
        if (refund == null) {
            return null;
        }
        return RefundResponse.builder()
                .id(refund.getId())
                .orderId(refund.getOrder().getId())
                .status(refund.getStatus())
                .amount(refund.getAmount() != null ? refund.getAmount().getAmount() : null)
                .reason(refund.getReason())
                .requestedBy(refund.getRequestedBy())
                .requestDate(refund.getRequestDate())
                .approvedDate(refund.getApprovedDate())
                .rejectedDate(refund.getRejectedDate())
                .completedDate(refund.getCompletedDate())
                .approvedBy(refund.getApprovedBy())
                .rejectedBy(refund.getRejectedBy())
                .rejectionReason(refund.getRejectionReason())
                .bankName(refund.getBankName())
                .accountNumber(refund.getAccountNumber())
                .accountHolder(refund.getAccountHolder())
                .refundTransactionId(refund.getRefundTransactionId())
                .processingMemo(refund.getProcessingMemo())
                .createdDate(refund.getCreatedDate())
                .updatedDate(refund.getUpdatedDate())
                .build();
    }
}