package com.example.spring.exception;

import com.example.spring.entity.DeliveryStatus;

public class DeliveryException {


    /**
     * 배송 정보를 찾을 수 없는 예외
     */
    public static class DeliveryNotFoundException extends BusinessException {
        public DeliveryNotFoundException(Long id) {
            super("DELIVERY_NOT_FOUND", "배송 정보를 찾을 수 없습니다. ID: " + id);
        }

        public DeliveryNotFoundException(String message) {
            super("DELIVERY_NOT_FOUND", message);
        }
    }

    /**
     * 잘못된 배송 상태 예외
     */
    public static class InvalidDeliveryStateException extends BusinessException {
        public InvalidDeliveryStateException(String message) {
            super("INVALID_DELIVERY_STATE", message);
        }

        public InvalidDeliveryStateException(Long deliveryId, DeliveryStatus status) {
            super("INVALID_DELIVERY_STATE",
                    String.format("잘못된 배송 상태입니다. 배송 ID: %d, 현재 상태: %s", deliveryId, status));
        }
    }

    /**
     * 배송지 변경 불가능 예외
     */
    public static class AddressChangeNotAllowedException extends BusinessException {
        public AddressChangeNotAllowedException(String message) {
            super("ADDRESS_CHANGE_NOT_ALLOWED", message);
        }
    }
}
