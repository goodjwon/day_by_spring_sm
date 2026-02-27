package com.example.spring.domain.repository;

import com.example.spring.domain.model.Payment;
import com.example.spring.domain.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 주문 ID로 결제 조회
    Optional<Payment> findByOrderId(Long orderId);

    // 거래 ID로 결제 조회
    Optional<Payment> findByTransactionId(String transactionId);

    // 결제 상태로 조회
    List<Payment> findByStatus(PaymentStatus status);

    // 주문 ID와 상태로 조회
    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);

    // 환불 가능한 결제 조회 (완료 또는 부분환불)
    List<Payment> findByStatusIn(List<PaymentStatus> statuses);
}