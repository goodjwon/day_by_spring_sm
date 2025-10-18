package com.example.patten.observer.extra.listener;

import com.example.patten.observer.extra.event.UserRegisteredEvent;
import com.example.patten.observer.extra.services.PointService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PointListener {

    private final PointService pointService;

    public PointListener(PointService pointService) {
        this.pointService = pointService;
    }

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        System.out.println("  ⭐ [PointListener] 이벤트 수신!");
        pointService.grantSignupPoints(event.getUserId(), 1000);
    }
}