package com.example.spring.domain.event;

import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.Refund;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 환불 요청 이벤트
 */
@Getter
public class RefundRequestedEvent {

    private final Refund refund;
    private final LocalDateTime occurredAt;

    public RefundRequestedEvent(Refund refund) {
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

    public String getReason() {
        return refund.getReason();
    }

    public String getRequestedBy() {
        return refund.getRequestedBy();
    }
}