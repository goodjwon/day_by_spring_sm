package com.example.spring.exception;

import com.example.spring.entity.OrderStatus;

public class RefundException {
    /**
     * 주문을 찾을 수 없는 예외
     */
    public static class RefundNotFoundException extends BusinessException {
        public RefundNotFoundException(Long id) {
            super("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다. ID: " + id);
        }
    }

//    /**
//     * 주문 취소 불가능 예외
//     */
//    public static class RefundCancellationNotAllowedException extends BusinessException {
//        public RefundCancellationNotAllowedException(Long orderId, OrderStatus status) {
//            super("ORDER_CANCELLATION_NOT_ALLOWED",
//                    String.format("주문을 취소할 수 없습니다. 주문 ID: %d, 현재 상태: %s", orderId, status));
//        }
//
//        public OrderCancellationNotAllowedException(String message) {
//            super("ORDER_CANCELLATION_NOT_ALLOWED", message);
//        }
//    }

    /**
     * 잘못된 주문 상태 예외
     */
    public static class InvalidRefundStateException extends BusinessException {
        public InvalidRefundStateException(String message) {
            super("INVALID_ORDER_STATE", message);
        }
    }

    /**
     * 결제 금액 불일치 예외
     */
    public static class RefundAmountMismatchException extends BusinessException {
        public RefundAmountMismatchException(String message) {
            super("PAYMENT_AMOUNT_MISMATCH", message);
        }
    }
}
