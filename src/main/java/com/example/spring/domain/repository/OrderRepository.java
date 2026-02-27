package com.example.spring.domain.repository;

import com.example.spring.domain.model.Order;
import com.example.spring.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 상태별 조회
    List<Order> findByStatus(OrderStatus status);
    long countByStatus(OrderStatus status);

    // 페이징 조회
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // 회원별 주문 조회 (N+1 문제 해결)
    @Query("SELECT o FROM Order o WHERE o.member.id = :memberId ORDER BY o.orderDate DESC")
    List<Order> findByMemberId(@Param("memberId") Long memberId);

    // 날짜 범위 조회
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 금액 범위 조회 (Money의 amount 필드 사용)
    @Query("SELECT o FROM Order o WHERE o.totalAmount.amount BETWEEN :minAmount AND :maxAmount")
    List<Order> findByTotalAmountBetween(@Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount);

    // 도서 ID로 주문 조회
    List<Order> findByOrderItems_Book_Id(Long bookId);

    // 총 매출 집계 (취소 주문 제외) - Money의 amount 필드 사용
    @Query("SELECT COALESCE(SUM(o.totalAmount.amount - COALESCE(o.discountAmount.amount, 0)), 0) FROM Order o WHERE o.status <> :excludeStatus")
    BigDecimal calculateTotalRevenue(@Param("excludeStatus") OrderStatus excludeStatus);

    // 기간별 매출 집계 (취소 주문 제외) - Money의 amount 필드 사용
    @Query("SELECT COALESCE(SUM(o.totalAmount.amount - COALESCE(o.discountAmount.amount, 0)), 0) FROM Order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.status <> :excludeStatus")
    BigDecimal calculateRevenueByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("excludeStatus") OrderStatus excludeStatus);

    // 편의 메서드 - Optional을 처리하지 않고 직접 Order 반환
    default Order findOrderById(Long id) {
        return findById(id).orElse(null);
    }
}