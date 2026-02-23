package com.example.spring.presentation.controller;

import com.example.spring.application.dto.response.DeliveryResponse;
import com.example.spring.domain.model.DeliveryStatus;
import com.example.spring.application.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 배송 관리 REST API Controller
 */
@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * 배송 ID로 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable Long id) {
        log.debug("배송 조회 요청 - ID: {}", id);
        DeliveryResponse delivery = deliveryService.findById(id);
        return ResponseEntity.ok(delivery);
    }

    /**
     * 주문 ID로 배송 조회
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(@PathVariable Long orderId) {
        log.debug("주문별 배송 조회 요청 - Order ID: {}", orderId);
        DeliveryResponse delivery = deliveryService.findByOrderId(orderId);
        return ResponseEntity.ok(delivery);
    }

    /**
     * 운송장 번호로 배송 조회
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<DeliveryResponse> getDeliveryByTrackingNumber(@PathVariable String trackingNumber) {
        log.debug("운송장 번호로 배송 조회 요청 - Tracking Number: {}", trackingNumber);
        DeliveryResponse delivery = deliveryService.findByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(delivery);
    }

    /**
     * 상태별 배송 목록 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByStatus(@PathVariable DeliveryStatus status) {
        log.debug("상태별 배송 조회 요청 - 상태: {}", status);

        List<DeliveryResponse> deliveries = deliveryService.findByStatus(status);
        return ResponseEntity.ok(deliveries);
    }

    /**
     * 배송 시작
     */
    @PatchMapping("/{id}/start")
    public ResponseEntity<DeliveryResponse> startShipping(
            @PathVariable Long id,
            @RequestParam String trackingNumber,
            @RequestParam String courierCompany) {

        log.info("배송 시작 요청 - ID: {}, 운송장: {}, 택배사: {}", id, trackingNumber, courierCompany);
        DeliveryResponse delivery = deliveryService.startShipping(id, trackingNumber, courierCompany);
        return ResponseEntity.ok(delivery);
    }

    /**
     * 배송 완료
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<DeliveryResponse> completeDelivery(@PathVariable Long id) {
        log.info("배송 완료 요청 - ID: {}", id);
        DeliveryResponse delivery = deliveryService.completeDelivery(id);
        return ResponseEntity.ok(delivery);
    }

    /**
     * 배송 상태 변경
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam DeliveryStatus status) {

        log.info("배송 상태 변경 요청 - ID: {}, 상태: {}", id, status);
        DeliveryResponse delivery = deliveryService.updateDeliveryStatus(id, status);
        return ResponseEntity.ok(delivery);
    }

    /**
     * 배송지 주소 변경
     */
    @PatchMapping("/{id}/address")
    public ResponseEntity<DeliveryResponse> changeAddress(
            @PathVariable Long id,
            @RequestParam(required = false) String zipCode,
            @RequestParam String address,
            @RequestParam(required = false) String addressDetail) {

        log.info("배송지 주소 변경 요청 - ID: {}, 주소: {}", id, address);
        DeliveryResponse delivery = deliveryService.changeAddress(id, zipCode, address, addressDetail);
        return ResponseEntity.ok(delivery);
    }
}