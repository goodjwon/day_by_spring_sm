package com.example.spring.presentation.controller;

import com.example.spring.application.dto.response.PaymentResponse;
import com.example.spring.domain.model.PaymentStatus;
import com.example.spring.application.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 결제 관리 REST API Controller
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 ID로 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        log.debug("결제 조회 요청 - ID: {}", id);
        PaymentResponse payment = paymentService.findById(id);
        return ResponseEntity.ok(payment);
    }

    /**
     * 주문 ID로 결제 조회
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        log.debug("주문별 결제 조회 요청 - Order ID: {}", orderId);
        PaymentResponse payment = paymentService.findByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }

    /**
     * 상태별 결제 목록 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        log.debug("상태별 결제 조회 요청 - 상태: {}", status);

        List<PaymentResponse> payments = paymentService.findByStatus(status);
        return ResponseEntity.ok(payments);
    }

    /**
     * 결제 완료 처리
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<PaymentResponse> completePayment(
            @PathVariable Long id,
            @RequestParam String transactionId) {

        log.info("결제 완료 처리 요청 - ID: {}, Transaction ID: {}", id, transactionId);
        PaymentResponse payment = paymentService.completePayment(id, transactionId);
        return ResponseEntity.ok(payment);
    }

    /**
     * 결제 실패 처리
     */
    @PatchMapping("/{id}/fail")
    public ResponseEntity<PaymentResponse> failPayment(
            @PathVariable Long id,
            @RequestParam String reason) {

        log.info("결제 실패 처리 요청 - ID: {}, 사유: {}", id, reason);
        PaymentResponse payment = paymentService.failPayment(id, reason);
        return ResponseEntity.ok(payment);
    }

    /**
     * 결제 취소
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long id) {
        log.info("결제 취소 요청 - ID: {}", id);
        PaymentResponse payment = paymentService.cancelPayment(id);
        return ResponseEntity.ok(payment);
    }

    /**
     * 결제 환불
     */
    @PatchMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id,
            @RequestParam BigDecimal refundAmount) {

        log.info("결제 환불 요청 - ID: {}, 환불 금액: {}", id, refundAmount);
        PaymentResponse payment = paymentService.refundPayment(id, refundAmount);
        return ResponseEntity.ok(payment);
    }
}