package com.example.patten.observer.extra.services;

import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    public void trackSignup(Long userId, String email) {
        System.out.println("  📊 [Analytics] 가입 통계 기록 (userId: " + userId + ")");
    }
}