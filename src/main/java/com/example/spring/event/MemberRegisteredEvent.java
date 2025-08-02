package com.example.spring.event;


import com.example.spring.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 회원가입 이벤트.
 */
@Getter
@AllArgsConstructor
public class MemberRegisteredEvent {
    private final Member member;
    private final LocalDateTime occurredAt;;

    public MemberRegisteredEvent(Member member) {
        this.member = member;
        this.occurredAt = LocalDateTime.now();
    }
}
