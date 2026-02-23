package com.example.spring.application.dto.response;

import com.example.spring.domain.model.Delivery;
import com.example.spring.domain.model.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 배송 정보 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponse {
    private Long id;
    private Long orderId;

    // 수령인 정보
    private String recipientName;
    private String phoneNumber;

    // 배송지 주소
    private String zipCode;
    private String address;
    private String addressDetail;

    private String deliveryMemo;

    // 배송 상태
    private DeliveryStatus status;

    // 택배 정보
    private String trackingNumber;
    private String courierCompany;

    // 배송 날짜 정보
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime estimatedDeliveryDate;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /**
     * Delivery 엔티티를 DeliveryResponse로 변환
     */
    public static DeliveryResponse from(Delivery delivery) {
        if (delivery == null) {
            return null;
        }
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrder().getId())
                .recipientName(delivery.getRecipientName())
                .phoneNumber(delivery.getPhoneNumber())
                .zipCode(delivery.getZipCode())
                .address(delivery.getAddress())
                .addressDetail(delivery.getAddressDetail())
                .deliveryMemo(delivery.getDeliveryMemo())
                .status(delivery.getStatus())
                .trackingNumber(delivery.getTrackingNumber())
                .courierCompany(delivery.getCourierCompany())
                .shippedDate(delivery.getShippedDate())
                .deliveredDate(delivery.getDeliveredDate())
                .estimatedDeliveryDate(delivery.getEstimatedDeliveryDate())
                .createdDate(delivery.getCreatedDate())
                .updatedDate(delivery.getUpdatedDate())
                .build();
    }
}