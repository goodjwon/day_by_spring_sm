package com.example.spring.service;

import com.example.spring.domain.vo.Money;
import com.example.spring.dto.response.PaymentResponse;
import com.example.spring.entity.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    PaymentResponse findById(Long paymentId);
    PaymentResponse findByOrderId(Long orderId);
    List<PaymentResponse> findByStatus(PaymentStatus paymentStatus);
    PaymentResponse completePayment(Long paymentId, String transactionId);
    PaymentResponse failPayment(Long paymentId, String failureReason);
    PaymentResponse cancelPayment(Long paymentId);
    PaymentResponse refundPayment(Long paymentId, BigDecimal refundAmount);
}
