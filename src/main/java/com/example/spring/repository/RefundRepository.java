package com.example.spring.repository;

import com.example.spring.entity.Refund;
import com.example.spring.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long>, JpaSpecificationExecutor<Refund> {
    @Query("SELECT r FROM Refund r WHERE r.order.id = :orderId")
    List<Refund> findByOrderId(Long orderId);

    @Query("SELECT r FROM Refund r WHERE r.status = :status")
    List<Refund> findByStatus(@Param("status") RefundStatus status);

    @Query("SELECT r FROM Refund r WHERE r.requestedBy = :requestedBy")
    List<Refund> findByRequestedBy(@Param("requestedBy") String requestedBy);

    @Query("SELECT r FROM Refund r WHERE r.requestDate BETWEEN :startDate AND :endDate")
    List<Refund> findByRequestDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Refund r WHERE r.order.id = :orderId AND r.status = :status")
    List<Refund> findByOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") RefundStatus status);

    @Query("SELECT r FROM Refund r WHERE r.status = RefundStatus.REQUESTED AND r.refundDate IS NULL")
    List<Refund> findPendingRefunds();

    @Query("SELECT r FROM Refund r WHERE r.refundTransactionId = :refundTransactionId")
    Optional<Refund> findByRefundTransactionId(@Param("refundTransactionId") String TransactionId);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r " +
            "WHERE r.order.id = :orderId " +
            "AND r.status = RefundStatus.COMPLETED")
    BigDecimal calculateTotalRefundedAmount(@Param("orderId") Long orderId);
    }
