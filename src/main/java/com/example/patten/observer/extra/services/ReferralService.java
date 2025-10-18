package com.example.patten.observer.extra.services;

import org.springframework.stereotype.Service;

@Service
public class ReferralService {
    public void processReferral(String referralCode, Long userId) {
        System.out.println("  👥 [Referral] 추천인 처리: " + referralCode + " → userId: " + userId);
    }
}
