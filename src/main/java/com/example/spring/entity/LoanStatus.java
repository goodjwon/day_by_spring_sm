package com.example.spring.entity;

public enum LoanStatus {
    /**
     * 대여 중
     */
    ACTIVE("대여 중"),

    /**
     * 반납 완료
     */
    RETURNED("반납 완료"),

    /**
     * 연체
     */
    OVERDUE("연체"),

    /**
     * 취소됨
     */
    CANCELLED("취소됨");

    private final String description;

    LoanStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
