package com.example.patten.observer.extra.service;

import org.springframework.stereotype.Service;

@Service
public class SlackService {
    public void notifyNewSignup(String name, String email) {
        System.out.println("  💬 [Slack] 신규 회원 알림: " + name + " (" + email + ")");
    }
}
