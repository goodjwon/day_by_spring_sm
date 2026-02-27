package com.example.spring.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * 대여 기간 검증기
 */
public class LoanPeriodValidator implements ConstraintValidator<ValidLoanPeriod, Integer> {

    private static final Set<Integer> VALID_PERIODS = Set.of(7, 14, 21, 30);

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        // null은 다른 검증에서 처리 (@NotNull)
        if (value == null) {
            return true;
        }

        return VALID_PERIODS.contains(value);
    }
}