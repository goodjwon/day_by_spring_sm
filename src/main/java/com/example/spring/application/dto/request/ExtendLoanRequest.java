package com.example.spring.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대여 연장 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtendLoanRequest {

    @NotNull(message = "{validation.loan.days.required}")
    @Min(value = 1, message = "{validation.loan.days.min1}")
    @Max(value = 14, message = "{validation.loan.days.max14}")
    @Builder.Default
    private Integer days = 14;
}