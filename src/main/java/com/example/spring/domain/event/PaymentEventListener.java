package com.example.spring.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 결제 관련 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    /**
     * 결제 완료 이벤트 처리
     */
    @EventListener
    @Async
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 처리 - 결제ID: {}, 주문ID: {}, 금액: {}, 거래ID: {}",
                event.getPaymentId(), event.getOrderId(), event.getAmount(), event.getTransactionId());

        try {
            // TODO: 결제 완료 이메일/SMS 발송
            log.info("결제 완료 알림 발송 시뮬레이션 - 결제ID: {}", event.getPaymentId());

            // TODO: 영수증 생성
            log.info("영수증 생성 시뮬레이션 - 결제ID: {}, 거래ID: {}",
                    event.getPaymentId(), event.getTransactionId());

        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 실패 - 결제ID: {}", event.getPaymentId(), e);
        }
    }

    /**
     * 결제 실패 이벤트 처리
     */
    @EventListener
    @Async
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 처리 - 결제ID: {}, 주문ID: {}, 사유: {}",
                event.getPaymentId(), event.getOrderId(), event.getFailureReason());

        try {
            // TODO: 결제 실패 알림 발송
            log.info("결제 실패 알림 발송 시뮬레이션 - 결제ID: {}", event.getPaymentId());

            // TODO: 주문 상태 업데이트 또는 재시도 안내
            log.info("결제 재시도 안내 시뮬레이션 - 주문ID: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("결제 실패 이벤트 처리 실패 - 결제ID: {}", event.getPaymentId(), e);
        }
    }
}