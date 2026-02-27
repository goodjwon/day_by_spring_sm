package com.example.spring.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 주문 관련 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    /**
     * 주문 생성 이벤트 처리
     */
    @EventListener
    @Async
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 처리 - 주문ID: {}, 회원ID: {}, 발생시각: {}",
                event.getOrderId(), event.getMemberId(), event.getOccurredAt());

        try {
            // TODO: 주문 확인 이메일 발송
            log.info("주문 확인 이메일 발송 시뮬레이션 - 주문ID: {}", event.getOrderId());

            // TODO: 재고 확인 및 예약
            log.info("재고 예약 처리 시뮬레이션 - 주문ID: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("주문 생성 이벤트 처리 실패 - 주문ID: {}", event.getOrderId(), e);
        }
    }

    /**
     * 주문 확정 이벤트 처리
     */
    @EventListener
    @Async
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("주문 확정 이벤트 처리 - 주문ID: {}, 회원ID: {}, 발생시각: {}",
                event.getOrderId(), event.getMemberId(), event.getOccurredAt());

        try {
            // TODO: 배송 준비 시작 알림
            log.info("배송 준비 시작 알림 시뮬레이션 - 주문ID: {}", event.getOrderId());

            // TODO: 재고 차감 처리
            log.info("재고 차감 처리 시뮬레이션 - 주문ID: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("주문 확정 이벤트 처리 실패 - 주문ID: {}", event.getOrderId(), e);
        }
    }

    /**
     * 주문 취소 이벤트 처리
     */
    @EventListener
    @Async
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("주문 취소 이벤트 처리 - 주문ID: {}, 사유: {}, 발생시각: {}",
                event.getOrderId(), event.getReason(), event.getOccurredAt());

        try {
            // TODO: 취소 확인 이메일 발송
            log.info("주문 취소 확인 이메일 발송 시뮬레이션 - 주문ID: {}", event.getOrderId());

            // TODO: 재고 복원 처리
            log.info("재고 복원 처리 시뮬레이션 - 주문ID: {}", event.getOrderId());

            // TODO: 환불 프로세스 시작 (결제 완료된 경우)
            log.info("환불 프로세스 시작 시뮬레이션 - 주문ID: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("주문 취소 이벤트 처리 실패 - 주문ID: {}", event.getOrderId(), e);
        }
    }
}