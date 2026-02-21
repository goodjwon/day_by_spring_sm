package com.example.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class BookstoreConfig {

    // 개발 환경용 설정
    @Profile("dev")
    @Configuration
    public static class DevConfig {
        // 개발 환경 전용 Bean들
    }

    // 운영 환경용 설정
    @Profile("prod")
    @Configuration
    public static class ProdConfig {
        // 운영 환경 전용 Bean들
    }

    // 테스트 환경용 설정
    @Profile("test")
    @Configuration
    public static class TestConfig {
        // 테스트 환경 전용 Bean들
    }
}