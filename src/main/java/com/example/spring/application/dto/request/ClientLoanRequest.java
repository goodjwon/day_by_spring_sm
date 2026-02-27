package com.example.spring.application.dto.request;

import com.example.spring.validation.ValidLoanPeriod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 대출 신청 요청 DTO
 * POST /api/client/loans/request
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientLoanRequest {

    @NotNull(message = "{validation.loan.bookId.required}")
    private Long bookId;

    @ValidLoanPeriod
    @Builder.Default
    private Integer loanPeriod = 14;  // 대출 기간 (일) - 기본 14일
}