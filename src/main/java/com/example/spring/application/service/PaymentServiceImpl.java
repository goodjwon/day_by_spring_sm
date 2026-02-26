package com.example.spring.application.service;

import com.example.spring.domain.vo.Money;
import com.example.spring.application.dto.response.PaymentResponse;
import com.example.spring.domain.model.Payment;
import com.example.spring.domain.model.PaymentStatus;
import com.example.spring.domain.event.PaymentCompletedEvent;
import com.example.spring.domain.event.PaymentFailedEvent;
import com.example.spring.exception.PaymentException;
import com.example.spring.domain.repository.PaymentRepository;
import com.example.spring.application.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse findByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException("결제 정보를 찾을 수 없습니다: orderId=" + orderId));
        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse findById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(id));
        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> findByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponse completePayment(Long paymentId, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(paymentId));

        payment.complete(transactionId);
        Payment saved = paymentRepository.save(payment);

        // 결제 완료 이벤트 발행
        eventPublisher.publishEvent(new PaymentCompletedEvent(saved));

        return PaymentResponse.from(saved);
    }

    @Override
    @Transactional
    public PaymentResponse failPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(paymentId));

        payment.fail(reason);
        Payment saved = paymentRepository.save(payment);

        // 결제 실패 이벤트 발행
        eventPublisher.publishEvent(new PaymentFailedEvent(saved, reason));

        return PaymentResponse.from(saved);
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(paymentId));

        payment.cancel();
        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.from(saved);
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, BigDecimal refundAmount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException.PaymentNotFoundException(paymentId));

        payment.refund(Money.of(refundAmount));
        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.from(saved);
    }
}