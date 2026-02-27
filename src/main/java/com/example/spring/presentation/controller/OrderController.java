package com.example.spring.presentation.controller;

import com.example.spring.application.dto.request.CreateOrderRequest;
import com.example.spring.application.dto.response.OrderResponse;
import com.example.spring.domain.model.OrderStatus;
import com.example.spring.application.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 관리 REST API Controller
 */
@Tag(name = "Order", description = "주문 관리 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("주문 생성 요청: {}", request);
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * 전체 주문 목록 조회 (페이징)
     */
    @Operation(summary = "전체 주문 목록 조회 (페이징)", description = "모든 주문을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 당 항목 수", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "정렬 기준 (orderDate, totalAmount, status)", example = "orderDate")
            @RequestParam(defaultValue = "orderDate") String sortBy,

            @Parameter(description = "정렬 순서 (asc, desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        log.debug("전체 주문 목록 조회 (페이징) - page: {}, size: {}, sortBy: {}, sortOrder: {}",
                page, size, sortBy, sortOrder);

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderResponse> orders = orderService.findAllOrdersWithPagination(pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * 특정 주문 조회
     */
    @Operation(summary = "특정 주문 조회", description = "주문 ID로 특정 주문을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        log.debug("주문 조회 요청 - ID: {}", id);
        OrderResponse order = orderService.findOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 주문 확인
     */
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long id) {
        log.info("주문 확인 요청 - ID: {}", id);
        OrderResponse order = orderService.confirmOrder(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 주문 배송 시작
     */
    @PatchMapping("/{id}/ship")
    public ResponseEntity<OrderResponse> shipOrder(
            @PathVariable Long id,
            @RequestParam String trackingNumber,
            @RequestParam String courierCompany) {

        log.info("주문 배송 시작 요청 - ID: {}, 운송장: {}", id, trackingNumber);
        OrderResponse order = orderService.shipOrder(id, trackingNumber, courierCompany);
        return ResponseEntity.ok(order);
    }

    /**
     * 주문 배송 완료
     */
    @PatchMapping("/{id}/deliver")
    public ResponseEntity<OrderResponse> deliverOrder(@PathVariable Long id) {
        log.info("주문 배송 완료 요청 - ID: {}", id);
        OrderResponse order = orderService.deliverOrder(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 주문 취소
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "고객 요청") String reason) {

        log.info("주문 취소 요청 - ID: {}, 사유: {}", id, reason);
        OrderResponse order = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(order);
    }

    /**
     * 상태별 주문 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.debug("상태별 주문 조회 요청 - 상태: {}", status);

        List<OrderResponse> orders = orderService.findOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * 날짜 범위로 주문 조회
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<OrderResponse>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.debug("날짜 범위로 주문 조회 - 시작: {}, 종료: {}", startDate, endDate);

        List<OrderResponse> orders = orderService.findOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    /**
     * 금액 범위로 주문 조회
     */
    @GetMapping("/amount-range")
    public ResponseEntity<List<OrderResponse>> getOrdersByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {

        log.debug("금액 범위로 주문 조회 - 최소: {}, 최대: {}", minAmount, maxAmount);

        List<OrderResponse> orders = orderService.findOrdersByAmountRange(minAmount, maxAmount);
        return ResponseEntity.ok(orders);
    }

    /**
     * 특정 도서가 포함된 주문 조회
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByBook(@PathVariable Long bookId) {
        log.debug("도서별 주문 조회 - 도서 ID: {}", bookId);

        List<OrderResponse> orders = orderService.findOrdersByBookId(bookId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 주문 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<OrderStatistics> getOrderStatistics() {
        log.debug("주문 통계 조회");

        long totalOrders = orderService.getTotalOrdersCount();
        long pendingOrders = orderService.getOrdersCountByStatus(OrderStatus.PENDING);
        long confirmedOrders = orderService.getOrdersCountByStatus(OrderStatus.CONFIRMED);
        long shippedOrders = orderService.getOrdersCountByStatus(OrderStatus.SHIPPED);
        long deliveredOrders = orderService.getOrdersCountByStatus(OrderStatus.DELIVERED);
        long cancelledOrders = orderService.getOrdersCountByStatus(OrderStatus.CANCELLED);
        BigDecimal totalRevenue = orderService.getTotalRevenue();

        OrderStatistics statistics = OrderStatistics.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .confirmedOrders(confirmedOrders)
                .shippedOrders(shippedOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue)
                .build();

        return ResponseEntity.ok(statistics);
    }

    /**
     * 기간별 매출 조회
     */
    @GetMapping("/revenue")
    public ResponseEntity<RevenueResponse> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.debug("기간별 매출 조회 - 시작: {}, 종료: {}", startDate, endDate);

        BigDecimal revenue = orderService.getRevenueByDateRange(startDate, endDate);
        RevenueResponse response = new RevenueResponse(startDate, endDate, revenue);

        return ResponseEntity.ok(response);
    }

    // ====== Response DTOs ======

    /**
     * 주문 통계 응답 DTO
     */
    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderStatistics {
        private long totalOrders;
        private long pendingOrders;
        private long confirmedOrders;
        private long shippedOrders;
        private long deliveredOrders;
        private long cancelledOrders;
        private BigDecimal totalRevenue;
    }

    /**
     * 매출 응답 DTO
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class RevenueResponse {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BigDecimal revenue;
    }
}