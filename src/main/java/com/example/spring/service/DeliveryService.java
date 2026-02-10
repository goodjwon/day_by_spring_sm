package com.example.spring.service;

import com.example.spring.dto.response.DeliveryResponse;
import com.example.spring.entity.DeliveryStatus;

import java.util.List;

public interface DeliveryService {
    /**
    * 배송 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
    */
    DeliveryResponse findById(Long deliveryId);
    DeliveryResponse findByOrderId(Long orderId);
    DeliveryResponse findByTrackingNumber(String trackingNumber);
    List<DeliveryResponse> findByStatus(DeliveryStatus status);
    DeliveryResponse startShipping(Long deliveryId, String trackingNumber, String courierCompany);
    DeliveryResponse completeDelivery(Long deliveryId);
    DeliveryResponse updateDeliveryStatus(Long deliveryId, DeliveryStatus status);
    DeliveryResponse changeAddress(Long deliveryId, String zipCode, String address, String addressDetails);
}
