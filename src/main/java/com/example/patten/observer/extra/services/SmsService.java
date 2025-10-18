package com.example.patten.observer.extra.services;

import org.springframework.stereotype.Service;

@Service
public class SmsService {
    public void sendVerificationSms(String phone) {
        System.out.println("  📱 [SMS] 인증번호 발송: " + phone);
        System.out.println("     내용: [인증번호] 123456");
    }
}