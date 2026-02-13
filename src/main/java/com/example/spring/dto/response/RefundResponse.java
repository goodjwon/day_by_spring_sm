package com.example.spring.dto.response;

import com.example.spring.domain.vo.Money;
import com.example.spring.entity.Refund;
import com.example.spring.entity.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private String approvedBy;
    private String rejectedBy;
    private String rejectionReason;
    private String refundTransactionId;
    private String processingMemo;

    public static RefundResponse from(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .orderId(refund.getOrder().getId())
                .amount(refund.getAmount().getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .approvedBy(refund.getApprovedBy())
                .rejectedBy(refund.getRejectedBy())
                .rejectionReason(refund.getRejectionReason())
                .refundTransactionId(refund.getRefundTransactionId())
                .processingMemo(refund.getProcessingMemo())
                .build();
    }
}
