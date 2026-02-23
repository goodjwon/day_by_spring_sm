package com.example.spring.domain.repository;

import com.example.spring.domain.model.Delivery;
import com.example.spring.domain.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    // 주문 ID로 배송 조회
    Optional<Delivery> findByOrderId(Long orderId);

    // 운송장 번호로 배송 조회
    Optional<Delivery> findByTrackingNumber(String trackingNumber);

    // 배송 상태로 조회
    List<Delivery> findByStatus(DeliveryStatus status);

    // 수령인 이름으로 조회
    List<Delivery> findByRecipientNameContaining(String recipientName);

    // 택배사로 조회
    List<Delivery> findByCourierCompany(String courierCompany);

    // 주소 변경 가능 여부 확인 (배송 준비중인 배송만)
    List<Delivery> findByStatusAndOrderId(DeliveryStatus status, Long orderId);
}