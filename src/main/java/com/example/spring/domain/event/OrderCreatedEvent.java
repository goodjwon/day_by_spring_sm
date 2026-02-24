package com.example.spring.domain.event;

import com.example.spring.domain.model.Order;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주문 생성 이벤트
 */
@Getter
public class OrderCreatedEvent {

    private final Order order;
    private final LocalDateTime occurredAt;

    public OrderCreatedEvent(Order order) {
        this.order = order;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getOrderId() {
        return order.getId();
    }

    public Long getMemberId() {
        return order.getMember() != null ? order.getMember().getId() : null;
    }
}