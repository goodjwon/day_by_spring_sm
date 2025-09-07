package com.example.patten.observer.after;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void placeOrder(String productId, String address) {
        System.out.println("--- 주문 처리를 시작합니다 ---");
        System.out.println("주문이 성공적으로 완료되었습니다.");

        // 주문 완료 '이벤트'를 발행!
        // OrderService는 이제 재고, 배송 서비스를 전혀 모릅니다.
        System.out.println(">> 'OrderPlacedEvent'를 발행합니다...");
        eventPublisher.publishEvent(new OrderPlacedEvent(productId, address));
        System.out.println("--------------------------\n");
    }
}
