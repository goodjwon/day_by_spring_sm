package com.example.spring.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    private Long refundId;
    private Long orderId;
    private BigDecimal amount;
    private String reason;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private String requestedBy;
}
