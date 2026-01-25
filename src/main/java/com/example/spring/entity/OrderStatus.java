package com.example.spring.entity;

// 주문 상태 enum
public enum OrderStatus {
    PENDING,        //접수
    CONFIRMED,      //주문확인
    SHIPPED,        //배송시작
    DELIVERED,      //배송완료
    CANCELLED       //취소
}