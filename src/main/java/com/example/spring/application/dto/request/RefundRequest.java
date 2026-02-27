package com.example.spring.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 환불 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {

    @NotNull(message = "{validation.refund.orderId.required}")
    private Long orderId;

    @NotNull(message = "{validation.refund.amount.required}")
    @Positive(message = "{validation.refund.amount.positive}")
    private BigDecimal amount;

    @NotBlank(message = "{validation.refund.reason.required}")
    private String reason;

    // 환불 계좌 정보
    @NotBlank(message = "{validation.refund.bank.required}")
    private String bankName;

    @NotBlank(message = "{validation.refund.account.required}")
    private String accountNumber;

    @NotBlank(message = "{validation.refund.depositor.required}")
    private String accountHolder;

    private String requestedBy;  // 환불 요청자 (관리자가 대신 요청할 수도 있음)
}