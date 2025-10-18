package com.example.patten.observer.extra.listener;

import com.example.patten.observer.extra.event.UserRegisteredEvent;
import com.example.patten.observer.extra.services.ReferralService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ReferralListener {

    private final ReferralService referralService;

    public ReferralListener(ReferralService referralService) {
        this.referralService = referralService;
    }

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        if (event.getReferralCode() != null) {
            System.out.println("  ⭐ [ReferralListener] 이벤트 수신!");
            referralService.processReferral(event.getReferralCode(), event.getUserId());
        }
    }
}