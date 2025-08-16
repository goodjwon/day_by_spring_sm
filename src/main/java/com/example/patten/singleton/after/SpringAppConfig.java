package com.example.patten.singleton.after;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component // Spring 컨테이너가 이 클래스를 Bean으로 등록하고 관리하게 함
public class SpringAppConfig {

    @PostConstruct // 의존성 주입이 완료된 후 실행되는 초기화 메서드
    public void init() {
        System.out.println(">> SpringAppConfig Bean이 생성 및 초기화되었습니다. (Spring 방식)");
    }

    public void printConfig() {
        System.out.println("Spring Bean 설정을 출력합니다.");
    }
}
