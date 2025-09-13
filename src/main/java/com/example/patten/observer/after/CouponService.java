package com.example.patten.observer.after;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class CouponService {
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event){
        System.out.println("⭐️ [쿠폰] 'OrderPlacedEvent' 수신!");
        System.out.println("쿠폰 코드: " + event.getAddress() + "을(를) 사용합니다.");
    }
}
