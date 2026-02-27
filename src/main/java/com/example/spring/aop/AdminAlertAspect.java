package com.example.spring.aop;

import com.example.spring.exception.BookException;
import com.example.spring.application.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAlertAspect {

    private final EmailService emailService;

    /**
     * 재고 부족 예외 발생 시 관리자에게 알림 발송
     */
    @AfterThrowing(pointcut = "execution(* com.example.spring.application.service..*(..))", throwing = "ex")
    public void handleStockShortage(BookException.BookNotAvailableException ex) {
        log.error("재고 부족 감지! 관리자 알림 트리거됨.");

        String subject = "재고 부족 발생 알림";
        String message = String.format("서비스 실행 중 재고 부족 예외가 발생했습니다.\n" +
                        "예외 메시지: %s\n" +
                        "발생 시간: %s\n" +
                        "--> 창고 확인 및 재고 추가가 필요합니다.",
                ex.getMessage(), java.time.LocalDateTime.now());

        emailService.sendAdminAlert(subject, message);
    }
}