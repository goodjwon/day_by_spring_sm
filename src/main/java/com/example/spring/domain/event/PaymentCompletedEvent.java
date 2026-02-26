package com.example.spring.domain.event;

import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.Payment;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 결제 완료 이벤트
 */
@Getter
public class PaymentCompletedEvent {

    private final Payment payment;
    private final LocalDateTime occurredAt;

    public PaymentCompletedEvent(Payment payment) {
        this.payment = payment;
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

    public String getTransactionId() {
        return payment.getTransactionId();
    }
}