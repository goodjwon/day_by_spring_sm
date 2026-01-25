package com.example.spring.exception;

import com.example.spring.entity.OrderStatus;

/**
 * 주문 관련 예외 클래스들
 */
public class OrderException {

    /**
     * 주문을 찾을 수 없는 예외
     */
    public static class OrderNotFoundException extends BusinessException {
        public OrderNotFoundException(Long id) {
            super("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다. ID: " + id);
        }
    }

    /**
     * 주문 취소 불가능 예외
     */
    public static class OrderCancellationNotAllowedException extends BusinessException {
        public OrderCancellationNotAllowedException(Long orderId, OrderStatus status) {
            super("ORDER_CANCELLATION_NOT_ALLOWED",
                    String.format("주문을 취소할 수 없습니다. 주문 ID: %d, 현재 상태: %s", orderId, status));
        }

        public OrderCancellationNotAllowedException(String message) {
            super("ORDER_CANCELLATION_NOT_ALLOWED", message);
        }
    }

    /**
     * 잘못된 주문 상태 예외
     */
    public static class InvalidOrderStateException extends BusinessException {
        public InvalidOrderStateException(String message) {
            super("INVALID_ORDER_STATE", message);
        }
    }

    /**
     * 주문 항목이 비어있는 예외
     */
    public static class EmptyOrderItemsException extends BusinessException {
        public EmptyOrderItemsException() {
            super("EMPTY_ORDER_ITEMS", "주문 항목이 비어있습니다.");
        }
    }

    /**
     * 결제 금액 불일치 예외
     */
    public static class PaymentAmountMismatchException extends BusinessException {
        public PaymentAmountMismatchException(String message) {
            super("PAYMENT_AMOUNT_MISMATCH", message);
        }
    }
}