package com.example.spring.exception;

public class PaymentException {
    public static class PaymentNotFoundException extends BusinessException {
        public PaymentNotFoundException(Long id) {super("PAYMENT_NOT_FOUND", "결재수단을 찾을 수 없습니다 ID: " + id);}
    }

    public static class InvalidPaymentStateException extends BusinessException {
        public InvalidPaymentStateException(String message) {super("INVALID_PAYMENT_STATE", message);}
    }
}
