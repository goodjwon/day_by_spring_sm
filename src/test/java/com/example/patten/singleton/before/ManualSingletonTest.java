package com.example.patten.singleton.before;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class ManualSingletonTest {
    @Test
    void manualSingletonTest() {
        System.out.println("✨ 전통적인 싱글톤 패턴 테스트 ✨");
        AppConfig config1 = AppConfig.getInstance();
        AppConfig config2 = AppConfig.getInstance();

        System.out.println("첫 번째 객체: " + config1);
        System.out.println("두 번째 객체: " + config2);

        // 두 객체의 참조가 동일한지 확인
        assertSame(config1, config2, "두 객체는 동일한 인스턴스여야 합니다.");
        System.out.println(">> 검증 완료: 두 참조는 동일한 객체를 가리킵니다.");
    }
}
