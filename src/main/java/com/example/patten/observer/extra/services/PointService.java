package com.example.patten.observer.extra.services;

import org.springframework.stereotype.Service;

@Service
public class PointService {
    public void grantSignupPoints(Long userId, int points) {
        System.out.println("  🎁 [Point] 포인트 지급: " + points + "P (userId: " + userId + ")");
    }
}