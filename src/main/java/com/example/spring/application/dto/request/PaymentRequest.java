package com.example.spring.application.dto.request;

import com.example.spring.domain.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 결제 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "{validation.payment.method.required}")
    private PaymentMethod method;

    @NotNull(message = "{validation.payment.amount.required}")
    @Positive(message = "{validation.payment.amount.positive}")
    private BigDecimal amount;

    // PG사 정보
    private String pgProvider;  // 예: 토스페이먼츠, 나이스페이

    // 카드 결제 시 필요한 정보 (선택적)
    private String cardCompany;
    private String cardNumber;  // 마스킹된 카드번호
    private Integer installmentMonths;  // 할부 개월수
}