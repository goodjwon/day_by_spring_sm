package com.example.spring.entity;

public enum DeliveryStatus {
    PREPARING,     // 배송 준비중
    IN_TRANSIT,    // 배송중
    OUT_FOR_DELIVERY,  // 배송지 도착
    DELIVERED,     // 배송완료
    FAILED,        // 배송 실패
    RETURNED       // 반품
}
