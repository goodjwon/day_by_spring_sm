package com.example.spring.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 대출 관련 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoanEventListener {

    /**
     * 대출 생성 이벤트 처리
     */
    @EventListener
    @Async
    public void handleLoanCreated(LoanCreatedEvent event) {
        log.info("대출 생성 이벤트 처리 - 대출ID: {}, 회원ID: {}, 도서: {}, 반납예정일: {}",
                event.getLoanId(), event.getMemberId(), event.getBookTitle(), event.getDueDate());

        try {
            // TODO: 대출 확인 이메일 발송
            log.info("대출 확인 이메일 발송 시뮬레이션 - 대출ID: {}", event.getLoanId());

            // TODO: 반납일 리마인더 스케줄 등록
            log.info("반납일 리마인더 스케줄 등록 시뮬레이션 - 대출ID: {}, 반납예정일: {}",
                    event.getLoanId(), event.getDueDate());

        } catch (Exception e) {
            log.error("대출 생성 이벤트 처리 실패 - 대출ID: {}", event.getLoanId(), e);
        }
    }

    /**
     * 도서 반납 이벤트 처리
     */
    @EventListener
    @Async
    public void handleLoanReturned(LoanReturnedEvent event) {
        log.info("도서 반납 이벤트 처리 - 대출ID: {}, 회원ID: {}, 도서: {}, 연체여부: {}",
                event.getLoanId(), event.getMemberId(), event.getBookTitle(), event.isWasOverdue());

        try {
            // TODO: 반납 확인 이메일 발송
            log.info("반납 확인 이메일 발송 시뮬레이션 - 대출ID: {}", event.getLoanId());

            if (event.isWasOverdue()) {
                // TODO: 연체료 안내
                log.info("연체료 안내 시뮬레이션 - 대출ID: {}, 연체료: {}",
                        event.getLoanId(), event.getOverdueFee());
            }

            // TODO: 도서 대기자에게 알림 (있는 경우)
            log.info("도서 대기자 알림 시뮬레이션 - 도서ID: {}", event.getBookId());

        } catch (Exception e) {
            log.error("도서 반납 이벤트 처리 실패 - 대출ID: {}", event.getLoanId(), e);
        }
    }

    /**
     * 대출 연체 이벤트 처리
     */
    @EventListener
    @Async
    public void handleLoanOverdue(LoanOverdueEvent event) {
        log.info("대출 연체 이벤트 처리 - 대출ID: {}, 회원이메일: {}, 연체일수: {}, 연체료: {}",
                event.getLoanId(), event.getMemberEmail(), event.getOverdueDays(), event.getCurrentOverdueFee());

        try {
            // TODO: 연체 알림 이메일/SMS 발송
            log.info("연체 알림 발송 시뮬레이션 - 대출ID: {}, 수신자: {}",
                    event.getLoanId(), event.getMemberEmail());

            // TODO: 연체 정보 기록
            log.info("연체 정보 기록 시뮬레이션 - 대출ID: {}, 도서: {}, 반납예정일: {}",
                    event.getLoanId(), event.getBookTitle(), event.getDueDate());

        } catch (Exception e) {
            log.error("대출 연체 이벤트 처리 실패 - 대출ID: {}", event.getLoanId(), e);
        }
    }
}