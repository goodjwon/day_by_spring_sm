package com.example.spring.domain.model;

/**
 * 환불 상태
 */
public enum RefundStatus {
    REQUESTED,    // 요청됨
    APPROVED,     // 승인됨
    REJECTED,     // 거부됨
    PROCESSING,   // 처리중
    COMPLETED,    // 완료
    FAILED        // 실패
}