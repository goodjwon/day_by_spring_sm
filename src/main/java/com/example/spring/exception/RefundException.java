package com.example.spring.exception;

import com.example.spring.domain.model.RefundStatus;

/**
 * 환불 관련 예외 클래스들
 */
public class RefundException {

    /**
     * 환불 정보를 찾을 수 없는 예외
     */
    public static class RefundNotFoundException extends BusinessException {
        public RefundNotFoundException(Long id) {
            super("REFUND_NOT_FOUND", ErrorMessages.refundNotFound(id));
        }

        public RefundNotFoundException(String message) {
            super("REFUND_NOT_FOUND", message);
        }
    }

    /**
     * 잘못된 환불 상태 예외
     */
    public static class InvalidRefundStateException extends BusinessException {
        public InvalidRefundStateException(String message) {
            super("INVALID_REFUND_STATE", message);
        }

        public InvalidRefundStateException(Long refundId, RefundStatus status) {
            super("INVALID_REFUND_STATE",
                    String.format("잘못된 환불 상태입니다. 환불 ID: %d, 현재 상태: %s", refundId, status));
        }
    }

    /**
     * 환불 처리 실패 예외
     */
    public static class RefundProcessingFailedException extends BusinessException {
        public RefundProcessingFailedException(String message) {
            super("REFUND_PROCESSING_FAILED", message);
        }
    }
}