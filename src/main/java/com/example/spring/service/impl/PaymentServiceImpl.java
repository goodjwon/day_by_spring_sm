package com.example.spring.service.impl;

import com.example.spring.dto.response.PaymentResponse;
import com.example.spring.entity.Payment;
import com.example.spring.entity.PaymentStatus;
import com.example.spring.exception.PaymentException;
import com.example.spring.repository.PaymentRepository;
import com.example.spring.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentResponse findById(Long paymentId) {
        log.info("결재 정보 조회 요청 - ID {}", paymentId);
        log.info("결재 정보 조회 완료 - ID {}", paymentId);
        return PaymentResponse.from(paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(paymentId)));
    }

    @Override
    public PaymentResponse findByOrderId(Long orderId) {
        log.info("주문 ID로 결재 정보 조회 요청 - ID: {}", orderId);
        log.info("주문 ID로 결재 정보 조회 완료 - ID: {}", orderId);
        return PaymentResponse.from(paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(orderId)));
    }

    @Override
    public List<PaymentResponse> findByStatus(PaymentStatus paymentStatus) {
        log.info("상태별 결재 정보 목록 조회 요청 - status: {}", paymentStatus);
        List<Payment> payments = paymentRepository.findByStatus(paymentStatus);
        log.info("상태별 결재 정보 목록 조회 완료 - status: {}", paymentStatus);
        return payments.stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse completePayment(Long paymentId, String transactionId) {
        log.info("결재 성공 요청 - ID: {}, 거래 ID: {}", paymentId,  transactionId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(paymentId));
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
        log.info("결재 성공 완료  - ID: {}, 거래 ID: {}", paymentId,  transactionId);
        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse failPayment(Long paymentId, String failureReason) {
        log.info("결재 실패 요청 - ID: {}, 실패 원인: {}", paymentId, failureReason);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(paymentId));
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(failureReason);
        paymentRepository.save(payment);
        log.info("결재 실패 완료 - ID: {}", paymentId);
        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse cancelPayment(Long paymentId) {
        log.info("결재 취소 요청 - ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(paymentId));
        if (!payment.getStatus().equals(PaymentStatus.COMPLETED)) {
            throw new PaymentException.InvalidPaymentStateException("완료된 결재만 취소할 수 있습니다");
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
        log.info("결재 취소 완료 - ID: {}", paymentId);
        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse refundPayment(Long paymentId, BigDecimal refundAmount) {
        log.info("결재 환불 요청 - ID: {}, 환불 금액: {}", paymentId,  refundAmount);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(paymentId));
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        log.info("결재 환불 완료 - ID: {}, 환불 금액: {}", paymentId, refundAmount);
        return PaymentResponse.from(payment);
    }
}
