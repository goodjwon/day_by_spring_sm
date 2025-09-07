package com.example.patten.observer.after;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        System.out.println("⭐️ [재고] 'OrderPlacedEvent' 수신!");
        System.out.println("상품 ID: " + event.getAddress() + "의 재고를 차감합니다.");
    }
}
