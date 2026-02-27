package com.example.spring.domain.event;

import com.example.spring.domain.model.Order;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주문 취소 이벤트
 */
@Getter
public class OrderCancelledEvent {

    private final Order order;
    private final String reason;
    private final LocalDateTime occurredAt;

    public OrderCancelledEvent(Order order, String reason) {
        this.order = order;
        this.reason = reason;
        this.occurredAt = LocalDateTime.now();
    }

    public OrderCancelledEvent(Order order) {
        this(order, null);
    }

    public Long getOrderId() {
        return order.getId();
    }

    public Long getMemberId() {
        return order.getMember() != null ? order.getMember().getId() : null;
    }
}