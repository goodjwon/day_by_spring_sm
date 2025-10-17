package com.example.patten.observer.extra;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private final AtomicLong idGenerator = new AtomicLong(1);

    public User registerUser(String email, String name, String phone) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🟢 STAGE 1: 기본 회원가입 처리");
        System.out.println("=".repeat(60));

        // 사용자 저장
        User user = new User(email, name, phone);
        user.setId(idGenerator.getAndIncrement());

        System.out.println("✅ 회원가입 완료: " + user.getName() + " (" + user.getEmail() + ")");
        System.out.println("=".repeat(60) + "\n");

        return user;
    }
}
