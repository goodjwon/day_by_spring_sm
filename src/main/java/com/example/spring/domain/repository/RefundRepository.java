package com.example.spring.domain.repository;

import com.example.spring.domain.model.Refund;
import com.example.spring.domain.model.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    // 주문 ID로 환불 목록 조회 (부분환불 가능하므로 List)
    List<Refund> findByOrderId(Long orderId);

    // 환불 상태로 조회
    List<Refund> findByStatus(RefundStatus status);

    // 요청자로 조회
    List<Refund> findByRequestedBy(String requestedBy);

    // 특정 기간 동안의 환불 요청 조회
    List<Refund> findByRequestDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 주문 ID와 상태로 조회
    List<Refund> findByOrderIdAndStatus(Long orderId, RefundStatus status);

    // 승인 대기 중인 환불 조회
    @Query("SELECT r FROM Refund r WHERE r.status = 'REQUESTED' ORDER BY r.requestDate ASC")
    List<Refund> findPendingRefunds();

    // 환불 거래 ID로 조회
    Optional<Refund> findByRefundTransactionId(String transactionId);

    // 특정 주문의 총 환불 금액 계산 (Money의 amount 필드 사용)
    @Query("SELECT COALESCE(SUM(r.amount.amount), 0) FROM Refund r WHERE r.order.id = :orderId AND r.status = 'COMPLETED'")
    java.math.BigDecimal calculateTotalRefundedAmount(@Param("orderId") Long orderId);
}