package com.example.spring.application;

import com.example.spring.application.dto.response.DeliveryResponse;
import com.example.spring.domain.model.DeliveryStatus;

import java.util.List;

public interface DeliveryService {
    // 배송 조회
    DeliveryResponse findByOrderId(Long orderId);
    DeliveryResponse findById(Long id);
    DeliveryResponse findByTrackingNumber(String trackingNumber);
    List<DeliveryResponse> findByStatus(DeliveryStatus status);

    // 배송 처리
    DeliveryResponse startShipping(Long deliveryId, String trackingNumber, String courierCompany);
    DeliveryResponse completeDelivery(Long deliveryId);
    DeliveryResponse updateDeliveryStatus(Long deliveryId, DeliveryStatus status);

    // 배송지 변경
    DeliveryResponse changeAddress(Long deliveryId, String zipCode, String address, String addressDetail);
}