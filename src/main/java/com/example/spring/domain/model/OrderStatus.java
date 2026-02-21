package com.example.spring.domain.model;

// 주문 상태 enum
public enum OrderStatus {
    PENDING,        //접수
    CONFIRMED,      //주문 확인
    SHIPPED,        //배송 시작됨
    DELIVERED,      //배송 완료됨
    CANCELLED       //주문 취소됨
}