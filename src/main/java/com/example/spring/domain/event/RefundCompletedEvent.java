package com.example.spring.domain.event;

import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.Refund;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 환불 완료 이벤트
 */
@Getter
public class RefundCompletedEvent {

    private final Refund refund;
    private final LocalDateTime occurredAt;

    public RefundCompletedEvent(Refund refund) {
        this.refund = refund;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getRefundId() {
        return refund.getId();
    }

    public Long getOrderId() {
        return refund.getOrder() != null ? refund.getOrder().getId() : null;
    }

    public Money getAmount() {
        return refund.getAmount();
    }

    public String getRefundTransactionId() {
        return refund.getRefundTransactionId();
    }
}