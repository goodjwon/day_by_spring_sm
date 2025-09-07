package com.example.patten.observer.after;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ShippingService {
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        System.out.println("⭐️ [배송] 'OrderPlacedEvent' 수신!");
        System.out.println("주소: " + event.getAddress() + "로 배송을 준비합니다.");
    }
}
