package com.example.patten.observer.extra;

import com.example.patten.PattenApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest(classes = PattenApplication.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;
    
    @Test
    void stage1_기본_회원가입() {
        System.out.println("\n💬 PM: '회원가입 기능만 만들어주세요!'");
        System.out.println("👨‍💻 개발자: '네, 간단하네요!'\n");

        // When
        User user = userService.registerUser(
                "user1@example.com",
                "김개발",
                "010-1234-5678", ""
        );

        // Then
        assertNotNull(user);
        assertEquals("김개발", user.getName());

        System.out.println("✨ Stage 1 완료!");
        System.out.println("📊 현재 의존성: 0개");
        System.out.println("📏 코드 라인: 약 10줄");
        System.out.println("😊 만족도: ★★★★★ (완벽!)\n");
    }

    @Test
    void stage2_이메일_추가() {
        System.out.println("\n💬 PM: '환영 이메일도 보내주세요!'");
        System.out.println("👨‍💻 개발자: '네, 추가했습니다!'\n");

        // When
        User user = userService.registerUser(
                "user2@example.com",
                "박이메일",
                "010-2222-3333", ""
        );

        // Then
        assertNotNull(user);
        assertEquals("박이메일", user.getName());

        System.out.println("✨ Stage 2 완료!");
        System.out.println("📊 현재 의존성: 1개 (EmailService)");
        System.out.println("📏 코드 라인: 약 15줄");
        System.out.println("🙂 만족도: ★★★★☆ (아직 괜찮음)\n");
    }

    @Test
    void stage3_SMS_추가() {
        System.out.println("\n💬 PM: 'SMS 인증도 보내주세요!'");
        System.out.println("👨‍💻 개발자: '또요...? 네... 추가했습니다.'\n");

        // When
        User user = userService.registerUser(
                "user3@example.com",
                "이에스엠에스",
                "010-3333-4444", ""
        );

        // Then
        assertNotNull(user);
        assertEquals("이에스엠에스", user.getName());

        System.out.println("✨ Stage 3 완료!");
        System.out.println("📊 현재 의존성: 2개 (EmailService, SmsService)");
        System.out.println("📏 코드 라인: 약 20줄");
        System.out.println("😐 만족도: ★★★☆☆ (슬슬 복잡해지네...)\n");
    }

    @Test
    void stage4_요구사항_폭발() {
        System.out.println("\n💬 PM: '아 그리고요... 포인트, 통계, 추천인, 슬랙 알림도 다 해주세요!'");
        System.out.println("👨‍💻 개발자: '...😱 (멘붕)'\n");

        // When
        User user = userService.registerUser(
                "user4@example.com",
                "최폭발",
                "010-4444-5555",
                "FRIEND2024"
        );

        // Then
        assertNotNull(user);
        assertEquals("최폭발", user.getName());

        System.out.println("\n❌❌❌ 문제점 발견! ❌❌❌");
        System.out.println("📊 현재 의존성: 6개!!! (EmailService, SmsService, PointService,");
        System.out.println("                      AnalyticsService, ReferralService, SlackService)");
        System.out.println("📏 코드 라인: 약 50줄 (계속 증가 중...)");
        System.out.println("🔥 문제 1: UserService가 너무 많은 것을 알고 있음 (높은 결합도)");
        System.out.println("🔥 문제 2: 새 기능 추가 시 UserService를 계속 수정해야 함 (OCP 위반)");
        System.out.println("🔥 문제 3: 테스트 시 6개 Mock 객체 필요 (테스트 지옥)");
        System.out.println("🔥 문제 4: 한 서비스 에러 시 전체 회원가입 실패");
        System.out.println("😱 만족도: ★☆☆☆☆ (이건 아니다...)\n");

        System.out.println("💡 해결책이 필요합니다! → 옵저버 패턴으로 리팩토링!");
    }

    @Test
    void final_옵저버_패턴_성공() {
        System.out.println("\n🎉🎉🎉 옵저버 패턴으로 리팩토링! 🎉🎉🎉\n");
        System.out.println("💬 PM: '카카오톡 알림도 추가해주세요!'");
        System.out.println("👨‍💻 개발자: '네! 리스너만 추가하면 됩니다!' (자신감 뿜뿜)\n");

        // When
        User user = userService.registerUser(
                "final@example.com",
                "성공한개발자",
                "010-9999-8888",
                "BEST2024"
        );

        // Then
        assertNotNull(user);
        assertEquals("성공한개발자", user.getName());

        System.out.println("\n✅✅✅ 리팩토링 완료! ✅✅✅");
        System.out.println("📊 UserService 의존성: 1개만! (ApplicationEventPublisher)");
        System.out.println("📏 UserService 코드 라인: 약 15줄 (Stage 4: 50줄 → 70% 감소!)");
        System.out.println("\n🎯 개선 효과:");
        System.out.println("  ✅ 낮은 결합도: UserService는 부가 기능들을 전혀 모름");
        System.out.println("  ✅ OCP 준수: 새 기능 추가 시 기존 코드 수정 불필요");
        System.out.println("  ✅ 단일 책임: 각 리스너가 자기 일만 처리");
        System.out.println("  ✅ 테스트 용이: Mock 1개만 필요");
        System.out.println("  ✅ 확장 용이: 리스너만 추가하면 끝!");
        System.out.println("\n😊 만족도: ★★★★★ (완벽!)\n");
    }

    @Test
    void 비교_Stage4_vs_Final() {
        System.out.println("\n📊 Stage 4 vs Final 비교\n");
        System.out.println("=" .repeat(80));
        System.out.println(String.format("%-30s | %-20s | %-20s", "항목", "Stage 4 (Before)", "Final (After)"));
        System.out.println("=".repeat(80));
        System.out.println(String.format("%-30s | %-20s | %-20s", "의존성 개수", "6개", "1개"));
        System.out.println(String.format("%-30s | %-20s | %-20s", "UserService 코드 라인", "약 50줄", "약 15줄"));
        System.out.println(String.format("%-30s | %-20s | %-20s", "새 기능 추가 시", "UserService 수정", "Listener만 추가"));
        System.out.println(String.format("%-30s | %-20s | %-20s", "테스트 Mock 개수", "6개", "1개"));
        System.out.println(String.format("%-30s | %-20s | %-20s", "결합도", "높음 (강결합)", "낮음 (느슨한 결합)"));
        System.out.println(String.format("%-30s | %-20s | %-20s", "OCP (개방-폐쇄 원칙)", "위반", "준수"));
        System.out.println("=".repeat(80) + "\n");
    }
}
