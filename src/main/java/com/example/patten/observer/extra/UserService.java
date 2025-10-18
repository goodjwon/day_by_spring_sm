package com.example.patten.observer.extra;


import com.example.patten.observer.extra.service.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final AtomicLong idGenerator = new AtomicLong(1);

    private final EmailService emailService;

    private final SmsService smsService;
    private final AnalyticsService analyticsService;
    private final PointService pointService;
    private final ReferralService referralService;
    private final SlackService slackService;

    // 생성자 주입.
    public UserService(EmailService emailService, SmsService smsService, AnalyticsService analyticsService,
                       PointService pointService, ReferralService referralService, SlackService slackService) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.analyticsService = analyticsService;
        this.pointService = pointService;
        this.referralService = referralService;
        this.slackService = slackService;
    }

    // 1. 회원 가입 로직을 만들어주세요~!!! => 이메일과, 이름, 전화번호만 받아서 회원 가입할께요~!..
    // 보안?
    // 입력필드는? 정보는 어떤걸 받을꺼야? id는 몰로 할꺼야...
    // 서비스만 만들고 컨트롤러, 레파지토리는 있다고 친다. 실행은 테스트 코드로만 실행한다. => 디자인패턴 리마인드 하는 시간이기때문에 과감하게 생략.
    // 2. 회원가입 하면 회원가입 환영 이메일도 발송해주세요~!!!
    public User registerUser(String email, String name, String phone, String referralCode) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🟢 STAGE 1: 기본 회원가입 처리");
        System.out.println("=".repeat(60));

        // 1. 사용자 저장
        User user = new User(email, name, phone);
        user.setId(idGenerator.incrementAndGet());

        // 2. 이메일 발송
        emailService.sendWelcomeEmail(email, name);

        // 3. SMS 발송
        smsService.sendVerificationSms(phone);


        // 4. 포인트 지급 (가입 포인트 1000 점)
        pointService.grantSignupPoints(user.getId(), 1000);

        // 5. 통계 기록
        analyticsService.trackSignup(user.getId(), email);

        // 6. 추천인 처리 (있을 수도 없을 수도 있음)
        if(referralCode!=null && !"".equals(referralCode)) {
            referralService.processReferral(referralCode, user.getId());
        }


        // 7. 슬랙 알림
        slackService.notifyNewSignup(email, name);

        System.out.println("✅ 회원가입 완료: " + user.getName() + " (" + user.getEmail() + ")");

        return user;
    }

}
