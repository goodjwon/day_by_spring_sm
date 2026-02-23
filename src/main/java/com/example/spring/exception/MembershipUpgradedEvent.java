package com.example.spring.domain.event;

import com.example.spring.domain.model.Member;
import com.example.spring.domain.model.MembershipType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 멤버십 업그레이드 이벤트
 */
@Getter
@AllArgsConstructor
public class MembershipUpgradedEvent {

    private final Member member;
    private final MembershipType previousType;
    private final MembershipType newType;
    private final LocalDateTime occurredAt;

    public MembershipUpgradedEvent(Member member, MembershipType previousType, MembershipType newType) {
        this.member = member;
        this.previousType = previousType;
        this.newType = newType;
        this.occurredAt = LocalDateTime.now();
    }
}