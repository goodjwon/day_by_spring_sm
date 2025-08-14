package com.example.patten.singleton.after;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertSame;

// 테스트에 필요한 클래스만 로딩하여 가볍게 테스트
@SpringBootTest(classes = SpringAppConfig.class)
public class SpringSingletonTest {

    @Autowired
    private ApplicationContext applicationContext; // Spring 컨테이너

    @Test
    void springSingletonTest() {
        System.out.println("\n✨ Spring 컨테이너의 싱글톤 테스트 ✨");

        // 컨테이너에서 Bean을 두 번 조회
        SpringAppConfig config1 = applicationContext.getBean(SpringAppConfig.class);
        SpringAppConfig config2 = applicationContext.getBean(SpringAppConfig.class);

        System.out.println("첫 번째로 가져온 Bean: " + config1);
        System.out.println("두 번째로 가져온 Bean: " + config2);

        // 두 객체의 참조(주소값)가 동일한지 확인
        assertSame(config1, config2, "Spring 컨테이너는 기본적으로 Bean을 싱글톤으로 관리합니다.");
        System.out.println(">> 검증 완료: 두 참조는 완벽히 동일한 객체입니다. Spring이 싱글톤을 보장합니다!");
    }
}
