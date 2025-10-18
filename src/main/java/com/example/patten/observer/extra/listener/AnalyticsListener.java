package com.example.patten.observer.extra.listener;

import com.example.patten.observer.extra.event.UserRegisteredEvent;
import com.example.patten.observer.extra.services.AnalyticsService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsListener {

    private final AnalyticsService analyticsService;

    public AnalyticsListener(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        System.out.println("  ⭐ [AnalyticsListener] 이벤트 수신!");
        analyticsService.trackSignup(event.getUserId(), event.getEmail());
    }
}