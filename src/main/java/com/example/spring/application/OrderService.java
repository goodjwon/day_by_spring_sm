package com.example.spring.application;

import com.example.spring.application.dto.request.CreateOrderRequest;
import com.example.spring.application.dto.response.OrderResponse;
import com.example.spring.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    // 주문 생성 및 조회
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse findOrderById(Long id);
    List<OrderResponse> findAllOrders();

    // 페이징 조회
    Page<OrderResponse> findAllOrdersWithPagination(Pageable pageable);
    Page<OrderResponse> findOrdersByStatusWithPagination(OrderStatus status, Pageable pageable);

    // 주문 상태 관리
    OrderResponse confirmOrder(Long id);
    OrderResponse shipOrder(Long id, String trackingNumber, String courierCompany);
    OrderResponse deliverOrder(Long id);
    OrderResponse cancelOrder(Long id, String reason);

    // 주문 검색
    List<OrderResponse> findOrdersByStatus(OrderStatus status);
    List<OrderResponse> findOrdersByMemberId(Long memberId);
    List<OrderResponse> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<OrderResponse> findOrdersByAmountRange(BigDecimal minAmount, BigDecimal maxAmount);
    List<OrderResponse> findOrdersByBookId(Long bookId);

    // 주문 통계
    long getTotalOrdersCount();
    long getOrdersCountByStatus(OrderStatus status);
    BigDecimal getTotalRevenue();
    BigDecimal getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}