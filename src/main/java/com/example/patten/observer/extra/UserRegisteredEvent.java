package com.example.patten.observer.extra;

import java.time.LocalDateTime;

/**
 * 도메인 이벤트: "회원가입이 완료되었다"는 사실을 담은 불변 객체
 */
public class UserRegisteredEvent {
    private final Long userId;
    private final String email;
    private final String name;
    private final String phone;
    private final String referralCode;
    private final LocalDateTime occurredAt;

    public UserRegisteredEvent(Long userId, String email, String name,
                               String phone, String referralCode) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.referralCode = referralCode;
        this.occurredAt = LocalDateTime.now();
    }

    // Getters
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getReferralCode() { return referralCode; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}