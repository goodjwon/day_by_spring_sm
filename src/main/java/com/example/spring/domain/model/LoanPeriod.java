package com.example.spring.domain.model;

import com.example.spring.exception.ErrorMessages;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 대여 기간 선택 옵션
 */
@Getter
@RequiredArgsConstructor
public enum LoanPeriod {
    ONE_WEEK(7, "1주일"),
    TWO_WEEKS(14, "2주일"),
    THREE_WEEKS(21, "3주일"),
    ONE_MONTH(30, "1개월");

    @JsonValue
    private final int days;
    private final String description;

    /**
     * 일수로부터 LoanPeriod 찾기
     */
    public static LoanPeriod fromDays(int days) {
        for (LoanPeriod period : values()) {
            if (period.days == days) {
                return period;
            }
        }
        throw new IllegalArgumentException(ErrorMessages.invalidLoanPeriod(days));
    }
}