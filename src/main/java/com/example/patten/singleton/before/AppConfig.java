package com.example.patten.singleton.before;

public class AppConfig {
    // 1. 클래스 로딩 시점에 유일한 인스턴스를 생성
    private static final AppConfig instance = new AppConfig();

    // 2. private 생성자로 외부에서의 생성을 막음
    private AppConfig() {
        // 설정 로딩 등 초기화 로직
        System.out.println(">> AppConfig 객체가 생성되었습니다. (전통 방식)");
    }

    // 3. 유일한 인스턴스에 접근할 수 있는 public static 메서드 제공
    public static AppConfig getInstance() {
        return instance;
    }

    public void printConfig() {
        System.out.println("애플리케이션 설정을 출력합니다.");
    }
}
