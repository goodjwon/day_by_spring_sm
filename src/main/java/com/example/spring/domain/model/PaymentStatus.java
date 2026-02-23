package com.example.spring.domain.model;

/**
 * 결제 상태
 */
public enum PaymentStatus {
    PENDING,      // 대기중
    COMPLETED,    // 완료
    FAILED,       // 실패
    CANCELLED,    // 취소
    REFUNDED,     // 환불완료
    PARTIAL_REFUNDED  // 부분환불
}