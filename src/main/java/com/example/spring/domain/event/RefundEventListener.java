package com.example.spring.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 환불 관련 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundEventListener {

    /**
     * 환불 요청 이벤트 처리
     */
    @EventListener
    @Async
    public void handleRefundRequested(RefundRequestedEvent event) {
        log.info("환불 요청 이벤트 처리 - 환불ID: {}, 주문ID: {}, 금액: {}, 사유: {}",
                event.getRefundId(), event.getOrderId(), event.getAmount(), event.getReason());

        try {
            // TODO: 환불 요청 접수 확인 이메일 발송
            log.info("환불 요청 접수 확인 이메일 발송 시뮬레이션 - 환불ID: {}", event.getRefundId());

            // TODO: 관리자에게 환불 요청 알림
            log.info("관리자 환불 요청 알림 시뮬레이션 - 환불ID: {}, 요청자: {}",
                    event.getRefundId(), event.getRequestedBy());

        } catch (Exception e) {
            log.error("환불 요청 이벤트 처리 실패 - 환불ID: {}", event.getRefundId(), e);
        }
    }

    /**
     * 환불 완료 이벤트 처리
     */
    @EventListener
    @Async
    public void handleRefundCompleted(RefundCompletedEvent event) {
        log.info("환불 완료 이벤트 처리 - 환불ID: {}, 주문ID: {}, 금액: {}, 거래ID: {}",
                event.getRefundId(), event.getOrderId(), event.getAmount(), event.getRefundTransactionId());

        try {
            // TODO: 환불 완료 이메일 발송
            log.info("환불 완료 이메일 발송 시뮬레이션 - 환불ID: {}", event.getRefundId());

            // TODO: 환불 영수증 생성
            log.info("환불 영수증 생성 시뮬레이션 - 환불ID: {}, 거래ID: {}",
                    event.getRefundId(), event.getRefundTransactionId());

        } catch (Exception e) {
            log.error("환불 완료 이벤트 처리 실패 - 환불ID: {}", event.getRefundId(), e);
        }
    }
}