package com.example.patten.observer.extra.service;

// sendWelcomeEmail => XXX님 가입을 축하드립니다.
// 출력. out.println => 리턴값이 없다.
// 스프링 Bean 등록. Service 어노테이션은 XXXX

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public void sendWelcomeEmail(String email, String name) {
        System.out.println("  📧 [Email] 환영 이메일 발송: " + email);
        System.out.println("     제목: " + name + "님, 가입을 환영합니다!");
    }
}
