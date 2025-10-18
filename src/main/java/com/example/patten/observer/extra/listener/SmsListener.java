package com.example.patten.observer.extra.listener;

import com.example.patten.observer.extra.event.UserRegisteredEvent;
import com.example.patten.observer.extra.services.SmsService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SmsListener {

    private final SmsService smsService;

    public SmsListener(SmsService smsService) {
        this.smsService = smsService;
    }

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        System.out.println("  ⭐ [SmsListener] 이벤트 수신!");
        smsService.sendVerificationSms(event.getPhone());
    }
}
