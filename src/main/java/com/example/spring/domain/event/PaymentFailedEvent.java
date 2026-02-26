package com.example.spring.domain.event;

import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.Payment;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 결제 실패 이벤트
 */
@Getter
public class PaymentFailedEvent {

    private final Payment payment;
    private final String failureReason;
    private final LocalDateTime occurredAt;

    public PaymentFailedEvent(Payment payment, String failureReason) {
        this.payment = payment;
        this.failureReason = failureReason;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getPaymentId() {
        return payment.getId();
    }

    public Long getOrderId() {
        return payment.getOrder() != null ? payment.getOrder().getId() : null;
    }

    public Money getAmount() {
        return payment.getAmount();
    }
}