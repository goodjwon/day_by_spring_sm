package com.example.spring.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 대여 기간 검증 어노테이션
 * 허용된 값: 7, 14, 21, 30일
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoanPeriodValidator.class)
@Documented
public @interface ValidLoanPeriod {
    String message() default "대여 기간은 7, 14, 21, 30일 중 하나여야 합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}