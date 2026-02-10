package com.example.spring.service.impl;


import com.example.spring.domain.vo.Address;
import com.example.spring.dto.response.DeliveryResponse;
import com.example.spring.entity.Delivery;
import com.example.spring.entity.DeliveryStatus;
import com.example.spring.exception.DeliveryException;
import com.example.spring.repository.DeliveryRepository;
import com.example.spring.repository.MemberRepository;
import com.example.spring.repository.OrderRepository;
import com.example.spring.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;

    @Override
    public DeliveryResponse findById(Long deliveryId) {
        log.info("존재하는 배송 ID로 조회 요청 - ID: {}", deliveryId);
        log.info("존재하는 배송 ID로 조회 완료 - ID: {}", deliveryId);
        return DeliveryResponse.from(deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException(deliveryId)));
    }

    @Override
    public DeliveryResponse findByOrderId(Long orderId) {
        log.info("주문 ID로 배송 조회 요청 - ID: {}", orderId);
        log.info("주문 ID로 배송 조회 완료 - ID: {}", orderId);
        return DeliveryResponse.from(deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException(orderId)));
    }

    @Override
    public DeliveryResponse findByTrackingNumber(String trackingNumber) {
        log.info("운송장 번호로 배송 조회 요청 - trackingNumber: {}", trackingNumber);
        log.info("운송장 번호로 배송 조회 완료 - trackingNumber: {}", trackingNumber);
        return DeliveryResponse.from(deliveryRepository.findByTrackingNumber(trackingNumber)
        .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException(trackingNumber)));
    }

    @Override
    public List<DeliveryResponse> findByStatus(DeliveryStatus status) {
        log.info("상태별 배송 목록 조회 요청 - status: {}", status);
        List<Delivery> deliveryList = deliveryRepository.findByStatus(status);
        DeliveryResponse response = DeliveryResponse.from((Delivery) deliveryList);
        log.info("상태별 배송 목록 조회 완료 - status: {}", status);
        return List.of(response);
    }

    @Override
    @Transactional
    @Override
    public DeliveryResponse startShipping(Long deliveryId, String trackingNumber,
                                          String courierCompany) {
        log.info("배송 시작 요청 - ID: {}, 운송장 번호: {}, 배송사: {}", deliveryId, trackingNumber, courierCompany);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException(deliveryId));
        delivery.startShipping(trackingNumber, courierCompany);
        log.info("배송 시작 - ID: {}", deliveryId);
        return DeliveryResponse.from(delivery);
    }

    @Override
    @Transactional
    @Override
    public DeliveryResponse completeDelivery(Long deliveryId) {
        log.info("배송 완료 요청 - ID: {}", deliveryId);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException(deliveryId));
        delivery.delivered();
        log.info("배송 완료 - ID: {}", deliveryId);
        return DeliveryResponse.from(delivery);
    }

    @Override
    public DeliveryResponse updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
        log.info("배송 상태 업데이트 요청 - ID: {}, 상태: {}", deliveryId, status);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException(deliveryId));
        updateStatus(delivery, status);
        Delivery savedDelivery = deliveryRepository.save(delivery);
        log.info("배송 상태 업데이트 완료 - ID: {}, 상태: {}", deliveryId, status);
        return DeliveryResponse.from(savedDelivery);
    }

    public void updateStatus(Delivery delivery, DeliveryStatus newStatus) {
        if (newStatus == null) {
            return;
        }
        if (newStatus == DeliveryStatus.RETURNED) {
            delivery.updateStatus(newStatus);
            return;
        }
        delivery.setStatus(newStatus);
    }

    @Override
    public DeliveryResponse changeAddress(Long deliveryId, String zipCode, String address, String addressDetails) {
        log.info("배송지 주소 변경 요청 -  ID: {}, 운송장 번호: {}, 주소: {}, 상세주소: {}", deliveryId, zipCode, address, addressDetails);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException.DeliveryNotFoundException(deliveryId));
        if (!(delivery.getStatus() == DeliveryStatus.IN_TRANSIT)) {
            throw new DeliveryException.AddressChangeNotAllowedException("배송 준비중일 때만 변경 가능합니다");
        }
        log.info("배송지 주소 변경 완료 - ID: {}", deliveryId);
        return null;
    }

    public void changeDetail(Delivery delivery, String newZipCode, Address newAddress,
                             Address newAddressDetails) {
        if (newZipCode == null || newAddress == null || newAddressDetails == null) {
            return;
        }
        if (newZipCode.isEmpty() || newAddress.equals("") || newAddressDetails.equals("")) {
            throw new IllegalArgumentException("변경할 정보를 입력해 주세요");
        }
        delivery.setZipCode(newZipCode);
        delivery.setDeliveryAddress(newAddress);
        delivery.setDeliveryAddress(newAddressDetails);
    }
}
