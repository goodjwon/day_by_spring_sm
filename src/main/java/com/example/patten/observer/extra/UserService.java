package com.example.patten.observer.extra;

import com.example.patten.observer.extra.event.UserRegisteredEvent;
import com.example.patten.observer.extra.services.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ApplicationEventPublisher eventPublisher;

    public UserService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public User registerUser(String email, String name, String phone, String referralCode) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ FINAL: 옵저버 패턴으로 리팩토링 완료!");
        System.out.println("=".repeat(60));

        // 1. 핵심 비즈니스 로직: 사용자 저장 (UserService의 본업!)
        User user = new User(email, name, phone);
        user.setId(idGenerator.getAndIncrement());
        System.out.println("✅ 회원가입 완료: " + user.getName());

        // 2. 이벤트 발행 - "회원가입 완료되었어요~" 외치기
        // UserService는 누가 듣는지, 무엇을 하는지 전혀 모름!
        System.out.println("📢 UserRegisteredEvent 발행 중...\n");
        eventPublisher.publishEvent(
                new UserRegisteredEvent(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getPhone(),
                        referralCode
                )
        );

        System.out.println("=".repeat(60) + "\n");
        return user;
    }
}
