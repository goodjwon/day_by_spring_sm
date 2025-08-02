package com.example.spring.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 회원 관련 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEventListener {

    // TODO: EmailService 의존성 주입 (나중에 구현)
    // private final EmailService emailService;
    /**
     * 회원 가입 이벤트 처리 - 환영 이메일 발송
     */
    @EventListener
    @Async
    public void handleMemberRegistered(MemberRegisteredEvent event) {
        log.info("회원 가입 이벤트 처리 - 회원ID: {}, 이메일: {}",
                event.getMember().getId(), event.getMember().getEmail());

        try {
            // TODO: 실제 이메일 발송 로직 구현
            // emailService.sendWelcomeEmail(event.getMember());
            log.info("환영 이메일 발송 시뮬레이션 - 수신자: {}", event.getMember().getEmail());

            // 시뮬레이션을 위한 지연
            Thread.sleep(1000);

        } catch (Exception e) {
            log.error("환영 이메일 발송 실패 - 회원ID: {}", event.getMember().getId(), e);
        }
    }
}