package com.example.patten.observer.extra;

import com.example.patten.observer.extra.services.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private final AtomicLong idGenerator = new AtomicLong(1);
    // 😱 의존성이 6개!!!
    private final EmailService emailService;
    private final SmsService smsService;
    private final PointService pointService;
    private final AnalyticsService analyticsService;
    private final ReferralService referralService;
    private final SlackService slackService;

    // 😱 생성자 파라미터가 6개!!!
    public UserService(
            EmailService emailService,
            SmsService smsService,
            PointService pointService,
            AnalyticsService analyticsService,
            ReferralService referralService,
            SlackService slackService) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.pointService = pointService;
        this.analyticsService = analyticsService;
        this.referralService = referralService;
        this.slackService = slackService;
    }

    public User registerUser(String email, String name, String phone, String referralCode) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔴 STAGE 4: 회원가입 + 모든 부가 기능들...");
        System.out.println("=".repeat(60));

        // 1. 사용자 저장
        User user = new User(email, name, phone);
        user.setId(idGenerator.getAndIncrement());
        System.out.println("✅ 회원가입 완료: " + user.getName());

        // 2. 이메일 발송
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());

        // 3. SMS 발송
        smsService.sendVerificationSms(user.getPhone());

        // 4. 포인트 지급
        pointService.grantSignupPoints(user.getId(), 1000);

        // 5. 통계 기록
        analyticsService.trackSignup(user.getId(), user.getEmail());

        // 6. 추천인 처리
        if (referralCode != null) {
            referralService.processReferral(referralCode, user.getId());
        }

        // 7. 슬랙 알림
        slackService.notifyNewSignup(user.getName(), user.getEmail());

        System.out.println("=".repeat(60) + "\n");
        return user;
    }
}
