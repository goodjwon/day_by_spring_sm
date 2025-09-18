package com.example.patten.proxy.after;

// AopProxyTest.java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// EventService와 PerformanceAspect를 Bean으로 등록
@SpringBootTest(classes = {EventService.class, PerformanceAspect.class})
public class AopProxyTest {

    @Autowired
    private EventService eventService;

    @Test
    void proxyTest() {
        System.out.println("\n✨ Spring AOP (프록시 패턴) 테스트 ✨");
        eventService.processEvent("애플리케이션 로그인");
    }
}