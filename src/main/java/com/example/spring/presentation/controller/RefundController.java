package com.example.spring.presentation.controller;

import com.example.spring.application.dto.request.RefundRequest;
import com.example.spring.application.dto.response.RefundResponse;
import com.example.spring.domain.model.RefundStatus;
import com.example.spring.application.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 환불 관리 REST API Controller
 */
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Slf4j
public class RefundController {

    private final RefundService refundService;

    /**
     * 환불 요청 생성
     */
    @PostMapping
    public ResponseEntity<RefundResponse> createRefund(@RequestBody RefundRequest request) {
        log.info("환불 요청 생성 - 주문 ID: {}, 금액: {}", request.getOrderId(), request.getAmount());
        RefundResponse refund = refundService.createRefund(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(refund);
    }

    /**
     * 환불 ID로 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<RefundResponse> getRefundById(@PathVariable Long id) {
        log.debug("환불 조회 요청 - ID: {}", id);
        RefundResponse refund = refundService.findById(id);
        return ResponseEntity.ok(refund);
    }

    /**
     * 주문 ID로 환불 목록 조회
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<RefundResponse>> getRefundsByOrderId(@PathVariable Long orderId) {
        log.debug("주문별 환불 조회 요청 - Order ID: {}", orderId);

        List<RefundResponse> refunds = refundService.findByOrderId(orderId);
        return ResponseEntity.ok(refunds);
    }

    /**
     * 상태별 환불 목록 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RefundResponse>> getRefundsByStatus(@PathVariable RefundStatus status) {
        log.debug("상태별 환불 조회 요청 - 상태: {}", status);

        List<RefundResponse> refunds = refundService.findByStatus(status);
        return ResponseEntity.ok(refunds);
    }

    /**
     * 승인 대기 중인 환불 목록 조회
     */
    @GetMapping("/pending")
    public ResponseEntity<List<RefundResponse>> getPendingRefunds() {
        log.debug("승인 대기 중인 환불 조회 요청");

        List<RefundResponse> refunds = refundService.findPendingRefunds();
        return ResponseEntity.ok(refunds);
    }

    /**
     * 환불 승인
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<RefundResponse> approveRefund(
            @PathVariable Long id,
            @RequestParam String approver) {

        log.info("환불 승인 요청 - ID: {}, 승인자: {}", id, approver);
        RefundResponse refund = refundService.approveRefund(id, approver);
        return ResponseEntity.ok(refund);
    }

    /**
     * 환불 거부
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<RefundResponse> rejectRefund(
            @PathVariable Long id,
            @RequestParam String rejecter,
            @RequestParam String reason) {

        log.info("환불 거부 요청 - ID: {}, 거부자: {}, 사유: {}", id, rejecter, reason);
        RefundResponse refund = refundService.rejectRefund(id, rejecter, reason);
        return ResponseEntity.ok(refund);
    }

    /**
     * 환불 처리 시작
     */
    @PatchMapping("/{id}/start-processing")
    public ResponseEntity<RefundResponse> startProcessing(@PathVariable Long id) {
        log.info("환불 처리 시작 요청 - ID: {}", id);
        RefundResponse refund = refundService.startProcessing(id);
        return ResponseEntity.ok(refund);
    }

    /**
     * 환불 완료
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<RefundResponse> completeRefund(
            @PathVariable Long id,
            @RequestParam String transactionId) {

        log.info("환불 완료 요청 - ID: {}, Transaction ID: {}", id, transactionId);
        RefundResponse refund = refundService.completeRefund(id, transactionId);
        return ResponseEntity.ok(refund);
    }

    /**
     * 환불 실패 처리
     */
    @PatchMapping("/{id}/fail")
    public ResponseEntity<RefundResponse> failRefund(
            @PathVariable Long id,
            @RequestParam String memo) {

        log.info("환불 실패 처리 요청 - ID: {}, 메모: {}", id, memo);
        RefundResponse refund = refundService.failRefund(id, memo);
        return ResponseEntity.ok(refund);
    }

    /**
     * 주문의 총 환불 금액 조회
     */
    @GetMapping("/order/{orderId}/total-amount")
    public ResponseEntity<TotalRefundAmountResponse> getTotalRefundedAmount(@PathVariable Long orderId) {
        log.debug("주문 총 환불 금액 조회 요청 - Order ID: {}", orderId);

        BigDecimal totalAmount = refundService.getTotalRefundedAmount(orderId);
        TotalRefundAmountResponse response = new TotalRefundAmountResponse(orderId, totalAmount);

        return ResponseEntity.ok(response);
    }

    // ====== Response DTOs ======

    /**
     * 총 환불 금액 응답 DTO
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class TotalRefundAmountResponse {
        private Long orderId;
        private BigDecimal totalRefundedAmount;
    }
}