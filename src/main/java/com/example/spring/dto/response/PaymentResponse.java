package com.example.spring.dto.response;

import com.example.spring.domain.vo.Money;
import com.example.spring.entity.Payment;
import com.example.spring.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String transactionId;
    private PaymentStatus status;
    private Money refundedAmount;
    private String failureReason;

    public static PaymentResponse from(Payment payment){
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .refundedAmount(payment.getRefundedAmount())
                .failureReason(payment.getFailureReason())
                .build();
    }
}
