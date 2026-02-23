package com.example.spring.domain.model;

import com.example.spring.domain.vo.Address;
import com.example.spring.exception.DeliveryException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 배송 정보 엔티티
 */
@Entity
@Table(name = "deliveries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 정보 (1:1 관계)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 수령인 정보
    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String phoneNumber;

    // 배송지 주소
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zipCode", column = @Column(name = "zip_code")),
            @AttributeOverride(name = "address", column = @Column(name = "address", nullable = false)),
            @AttributeOverride(name = "addressDetail", column = @Column(name = "address_detail"))
    })
    private Address deliveryAddress;

    // 배송 메모
    @Column(length = 500)
    private String deliveryMemo;

    // 배송 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PREPARING;

    // 택배 정보
    private String trackingNumber;
    private String courierCompany;  // 예: CJ대한통운, 우체국택배, 로젠택배 등

    // 배송 날짜 정보
    private LocalDateTime shippedDate;      // 배송 시작일
    private LocalDateTime deliveredDate;    // 배송 완료일
    private LocalDateTime estimatedDeliveryDate;  // 배송 예정일

    // 생성 및 수정 일시
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // 비즈니스 로직 메서드
    public void startShipping(String trackingNumber, String courierCompany) {
        if (this.status != DeliveryStatus.PREPARING) {
            throw new DeliveryException.InvalidDeliveryStateException("배송 준비중인 상태에서만 배송을 시작할 수 있습니다.");
        }
        this.status = DeliveryStatus.IN_TRANSIT;
        this.trackingNumber = trackingNumber;
        this.courierCompany = courierCompany;
        this.shippedDate = LocalDateTime.now();
    }

    public void complete() {
        if (this.status != DeliveryStatus.OUT_FOR_DELIVERY &&
                this.status != DeliveryStatus.IN_TRANSIT) {
            throw new DeliveryException.InvalidDeliveryStateException("배송중인 상태에서만 완료할 수 있습니다.");
        }
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredDate = LocalDateTime.now();
    }

    public void updateStatus(DeliveryStatus newStatus) {
        this.status = newStatus;
    }

    public boolean isDelivered() {
        return this.status == DeliveryStatus.DELIVERED;
    }

    public boolean canChangeAddress() {
        // 배송 준비중일 때만 주소 변경 가능
        return this.status == DeliveryStatus.PREPARING;
    }

    public void changeAddress(String zipCode, String address, String addressDetail) {
        if (!canChangeAddress()) {
            throw new DeliveryException.AddressChangeNotAllowedException("배송 준비중일 때만 주소를 변경할 수 있습니다.");
        }
        this.deliveryAddress = Address.of(zipCode, address, addressDetail);
    }

    public void changeAddress(Address newAddress) {
        if (!canChangeAddress()) {
            throw new DeliveryException.AddressChangeNotAllowedException("배송 준비중일 때만 주소를 변경할 수 있습니다.");
        }
        this.deliveryAddress = newAddress;
    }

    // 하위 호환성을 위한 getter 메서드들
    public String getZipCode() {
        return deliveryAddress != null ? deliveryAddress.getZipCode() : null;
    }

    public String getAddress() {
        return deliveryAddress != null ? deliveryAddress.getAddress() : null;
    }

    public String getAddressDetail() {
        return deliveryAddress != null ? deliveryAddress.getAddressDetail() : null;
    }

    public String getFullAddress() {
        return deliveryAddress != null ? deliveryAddress.getFullAddress() : null;
    }

    /**
     * 주문에 연결 (package-private)
     */
    void attachToOrder(Order order) {
        this.order = order;
    }

    /**
     * 수령인 정보 변경
     */
    public void changeRecipient(String recipientName, String phoneNumber) {
        if (!canChangeAddress()) {
            throw new DeliveryException.AddressChangeNotAllowedException("배송 준비중일 때만 수령인 정보를 변경할 수 있습니다.");
        }
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
    }

    /**
     * 배송 메모 수정
     */
    public void updateMemo(String memo) {
        this.deliveryMemo = memo;
    }

    /**
     * 배송 예정일 설정
     */
    public void updateEstimatedDeliveryDate(LocalDateTime estimatedDate) {
        this.estimatedDeliveryDate = estimatedDate;
    }
}