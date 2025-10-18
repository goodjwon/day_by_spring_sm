package com.example.patten.observer.extra.listener;

import com.example.patten.observer.extra.event.UserRegisteredEvent;
import com.example.patten.observer.extra.services.SlackService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SlackListener {

    private final SlackService slackService;

    public SlackListener(SlackService slackService) {
        this.slackService = slackService;
    }

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        System.out.println("  ⭐ [SlackListener] 이벤트 수신!");
        slackService.notifyNewSignup(event.getName(), event.getEmail());
    }
}