package com.example.spring.exception;

import com.example.spring.domain.model.PaymentStatus;

/**
 * 결제 관련 예외 클래스들
 */
public class PaymentException {

    /**
     * 결제 정보를 찾을 수 없는 예외
     */
    public static class PaymentNotFoundException extends BusinessException {
        public PaymentNotFoundException(Long id) {
            super("PAYMENT_NOT_FOUND", ErrorMessages.paymentNotFound(id));
        }

        public PaymentNotFoundException(String message) {
            super("PAYMENT_NOT_FOUND", message);
        }
    }

    /**
     * 잘못된 결제 상태 예외
     */
    public static class InvalidPaymentStateException extends BusinessException {
        public InvalidPaymentStateException(String message) {
            super("INVALID_PAYMENT_STATE", message);
        }

        public InvalidPaymentStateException(Long paymentId, PaymentStatus status) {
            super("INVALID_PAYMENT_STATE",
                    String.format("잘못된 결제 상태입니다. 결제 ID: %d, 현재 상태: %s", paymentId, status));
        }
    }

    /**
     * 결제 금액 유효성 예외
     */
    public static class InvalidPaymentAmountException extends BusinessException {
        public InvalidPaymentAmountException(String message) {
            super("INVALID_PAYMENT_AMOUNT", message);
        }
    }

    /**
     * 결제 처리 실패 예외
     */
    public static class PaymentProcessingFailedException extends BusinessException {
        public PaymentProcessingFailedException(String message) {
            super("PAYMENT_PROCESSING_FAILED", message);
        }
    }
}