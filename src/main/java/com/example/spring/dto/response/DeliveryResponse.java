package com.example.spring.dto.response;

import com.example.spring.domain.vo.Address;
import com.example.spring.entity.Delivery;
import com.example.spring.entity.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {
    private Long id;
    private Long orderId;
    private DeliveryStatus status;
    private String recipientName;
    private String zipCode;
    private String address;
    private Address addressDetail;
    private String trackingNumber;
    private String courierCompany;

    public static DeliveryResponse from(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrder().getId())
                .status(delivery.getStatus())
                .recipientName(delivery.getRecipientName())
                .zipCode(delivery.getZipCode())
                .address(delivery.getDeliveryAddress())
                .addressDetail(delivery.getAddressDetail())
                .trackingNumber(delivery.getTrackingNumber())
                .build();
    }
}
