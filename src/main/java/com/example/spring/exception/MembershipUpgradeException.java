package com.example.spring.exception;

import com.example.spring.domain.model.MembershipType;

/**
 * 멤버십 업그레이드 관련 예외
 */
public class MembershipUpgradeException extends BusinessException {

    public MembershipUpgradeException(MembershipType currentType, MembershipType targetType) {
        super("MEMBERSHIP_UPGRADE_ERROR",
                String.format("멤버십 업그레이드가 불가능합니다. 현재: %s, 대상: %s",
                        currentType, targetType));
    }

    public MembershipUpgradeException(String message) {
        super("MEMBERSHIP_UPGRADE_ERROR", message);
    }
}