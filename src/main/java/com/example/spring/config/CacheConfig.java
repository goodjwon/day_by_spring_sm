package com.example.spring.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 캐시 설정
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 간단한 메모리 기반 캐시 매니저
     * 실제 운영환경에서는 Redis 등을 사용
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "members",           // 회원 정보 캐시
                "memberLoanLimits",  // 회원 대여 제한 정보 캐시
                "books",             // 도서 정보 캐시
                "statistics"         // 통계 정보 캐시
        );
    }
}