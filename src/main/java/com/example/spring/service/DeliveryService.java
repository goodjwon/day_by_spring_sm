package com.example.spring.service;

import com.example.spring.dto.response.DeliveryResponse;
import com.example.spring.entity.DeliveryStatus;

import java.util.List;

public interface DeliveryService {
    /**
    *
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
