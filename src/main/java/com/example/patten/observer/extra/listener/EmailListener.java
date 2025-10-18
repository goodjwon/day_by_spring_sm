package com.example.patten.observer.extra.listener;

import com.example.patten.observer.extra.UserRegisteredEvent;
import com.example.patten.observer.extra.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EmailListener {

    private final EmailService emailService;

    public EmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        System.out.println("  ⭐ [EmailListener] 이벤트 수신!");
        emailService.sendWelcomeEmail(event.getEmail(), event.getName());
    }


}
