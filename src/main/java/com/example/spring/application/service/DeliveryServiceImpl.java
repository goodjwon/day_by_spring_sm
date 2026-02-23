package com.example.spring.application.service;

import com.example.spring.application.dto.response.DeliveryResponse;
import com.example.spring.domain.model.Delivery;
import com.example.spring.domain.model.DeliveryStatus;
import com.example.spring.exception.DeliveryException;
import com.example.spring.domain.repository.DeliveryRepository;
import com.example.spring.application.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse findByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException("배송 정보를 찾을 수 없습니다: orderId=" + orderId));
        return DeliveryResponse.from(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse findById(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException(id));
        return DeliveryResponse.from(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse findByTrackingNumber(String trackingNumber) {
        Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException("배송 정보를 찾을 수 없습니다: trackingNumber=" + trackingNumber));
        return DeliveryResponse.from(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> findByStatus(DeliveryStatus status) {
        return deliveryRepository.findByStatus(status).stream()
                .map(DeliveryResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeliveryResponse startShipping(Long deliveryId, String trackingNumber, String courierCompany) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException(deliveryId));

        delivery.startShipping(trackingNumber, courierCompany);
        Delivery saved = deliveryRepository.save(delivery);
        return DeliveryResponse.from(saved);
    }

    @Override
    @Transactional
    public DeliveryResponse completeDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException(deliveryId));

        delivery.complete();
        Delivery saved = deliveryRepository.save(delivery);
        return DeliveryResponse.from(saved);
    }

    @Override
    @Transactional
    public DeliveryResponse updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException(deliveryId));

        delivery.updateStatus(status);
        Delivery saved = deliveryRepository.save(delivery);
        return DeliveryResponse.from(saved);
    }

    @Override
    @Transactional
    public DeliveryResponse changeAddress(Long deliveryId, String zipCode, String address, String addressDetail) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException(deliveryId));

        delivery.changeAddress(zipCode, address, addressDetail);
        Delivery saved = deliveryRepository.save(delivery);
        return DeliveryResponse.from(saved);
    }
}