package com.example.patten.observer.extra;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendWelcomeEmail(String email, String name) {
        System.out.println("  📧 [Email] 환영 이메일 발송: " + email);
        System.out.println("     제목: " + name + "님, 가입을 환영합니다!");
    }
}