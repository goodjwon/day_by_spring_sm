package com.example.patten.observer.extra.service;

import org.springframework.stereotype.Service;

// sendVerificationSms -> 리턴은 없고
// SpringBoot @Service 달아줘야 가고
// 출력만 하면 보낸 것으로 간다.
@Service
public class SmsService {
    public void sendVerificationSms(String phone) {
        System.out.println("  📱 [SMS] 인증번호 발송: " + phone);
        System.out.println("     내용: [인증번호] 123456");
    }
}
