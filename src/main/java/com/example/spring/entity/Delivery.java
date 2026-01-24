package com.example.spring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PREPARING;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private String address;

    private String addressDetail;

    @Column(length = 500)
    private String deliveryMemo;

    // 택배 정보
    private String trackingNumber;
    private String courierCompany;

    // 배송 날짜 정보
    private LocalDateTime shippedDate;      // 배송 시작일
    private LocalDateTime deliveredDate;    // 배송 완료일
    private LocalDateTime estimatedDeliveryDate;  // 배송 예정일

    // 생성 및 수정 일시
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;


    //todo 1.26 메서두 추가하기 과제.
    // onCreate
    // onUpdate
    // IN_TRANSIT
    // OUT_FOR_DELIVERY
    // DELIVERED
    // FAILED
    // RETURNED
}
