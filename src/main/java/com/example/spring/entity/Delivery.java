package com.example.spring.entity;

import com.example.spring.exception.DeliveryException;
import com.example.spring.exception.OrderException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
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
    // onCreate\
    @PrePersist
    protected  void onCreate(){
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }
    // onUpdate
    @PreUpdate
    protected  void onUpdate(){
        updatedDate = LocalDateTime.now();
    }
    // IN_TRANSIT
    public void transit(){
        if (status != DeliveryStatus.PREPARING) {
            throw new DeliveryException.InvalidDeliveryStateException("배송을 시작할 수 없는 주문입니다 현재 상태: " + this.status);
        }
        this.status = DeliveryStatus.IN_TRANSIT;
        this.shippedDate = LocalDateTime.now();
    }
    // OUT_FOR_DELIVERY
    public void outForDelivery() {
        if (status != DeliveryStatus.IN_TRANSIT) {
            throw new DeliveryException.InvalidDeliveryStateException("배송 중인 주문입니다" + this.status);
        }
        this.status = DeliveryStatus.OUT_FOR_DELIVERY;
        this.estimatedDeliveryDate = LocalDateTime.now().plusDays(3);
    }
    // DELIVERED
    public void delivered() {
        if (status != DeliveryStatus.OUT_FOR_DELIVERY) {
            throw new DeliveryException.InvalidDeliveryStateException("배송 완료처리를 하지 않은 주문입니다" + this.status);
        }
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredDate = LocalDateTime.now();
    }
    // FAILED
    public void failed() {
        this.status = DeliveryStatus.FAILED;
    }
    // RETURNED
    public void returned() {
        this.status = DeliveryStatus.RETURNED;
        this.deliveredDate = null;
    }

    public boolean canChangeAddress() {
        return status == DeliveryStatus.PREPARING;
    }
}
