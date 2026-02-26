package com.example.spring.application;

import com.example.spring.application.dto.response.PaymentResponse;
import com.example.spring.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    // 결제 조회
    PaymentResponse findByOrderId(Long orderId);
    PaymentResponse findById(Long id);
    List<PaymentResponse> findByStatus(PaymentStatus status);

    // 결제 처리
    PaymentResponse completePayment(Long paymentId, String transactionId);
    PaymentResponse failPayment(Long paymentId, String reason);
    PaymentResponse cancelPayment(Long paymentId);
    PaymentResponse refundPayment(Long paymentId, BigDecimal refundAmount);
}