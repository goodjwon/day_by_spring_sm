package com.example.spring.domain.service;

import com.example.spring.domain.vo.Money;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 연체료 계산 도메인 서비스
 *
 * 연체료 계산 로직을 중앙화하여 일관된 정책 적용을 보장합니다.
 * 정책 변경 시 이 클래스만 수정하면 됩니다.
 */
@Component
public class OverdueFeeCalculator {

    /**
     * 기본 일일 연체료 (1,000원)
     */
    private static final Money DAILY_FEE = Money.of(1000);

    /**
     * 최대 연체료 (도서 가격의 100% 또는 30,000원 중 작은 값)
     */
    private static final Money MAX_OVERDUE_FEE = Money.of(30000);

    /**
     * 연체 일수 계산
     *
     * @param dueDate 반납 예정일
     * @param returnDate 실제 반납일 (null이면 현재 시간 기준)
     * @return 연체 일수 (연체되지 않은 경우 0)
     */
    public long calculateOverdueDays(LocalDateTime dueDate, LocalDateTime returnDate) {
        LocalDateTime checkDate = returnDate != null ? returnDate : LocalDateTime.now();

        if (!checkDate.isAfter(dueDate)) {
            return 0;
        }

        return ChronoUnit.DAYS.between(dueDate, checkDate);
    }

    /**
     * 연체료 계산 (기본 정책)
     *
     * @param dueDate 반납 예정일
     * @param returnDate 실제 반납일 (null이면 현재 시간 기준)
     * @return 계산된 연체료
     */
    public Money calculate(LocalDateTime dueDate, LocalDateTime returnDate) {
        long overdueDays = calculateOverdueDays(dueDate, returnDate);

        if (overdueDays <= 0) {
            return Money.zero();
        }

        Money calculatedFee = DAILY_FEE.multiply((int) overdueDays);

        // 최대 연체료 제한 적용
        return calculatedFee.compareTo(MAX_OVERDUE_FEE) > 0 ? MAX_OVERDUE_FEE : calculatedFee;
    }

    /**
     * 연체료 계산 (도서 가격 기반 최대값 적용)
     *
     * @param dueDate 반납 예정일
     * @param returnDate 실제 반납일
     * @param bookPrice 도서 가격
     * @return 계산된 연체료 (도서 가격을 초과하지 않음)
     */
    public Money calculateWithBookPriceLimit(LocalDateTime dueDate, LocalDateTime returnDate, Money bookPrice) {
        Money calculatedFee = calculate(dueDate, returnDate);

        // 도서 가격을 초과하지 않도록 제한
        return calculatedFee.compareTo(bookPrice) > 0 ? bookPrice : calculatedFee;
    }

    /**
     * 연체 여부 확인
     *
     * @param dueDate 반납 예정일
     * @param returnDate 실제 반납일 (null이면 현재 시간 기준)
     * @return 연체 여부
     */
    public boolean isOverdue(LocalDateTime dueDate, LocalDateTime returnDate) {
        LocalDateTime checkDate = returnDate != null ? returnDate : LocalDateTime.now();
        return checkDate.isAfter(dueDate);
    }

    /**
     * 일일 연체료 조회
     */
    public Money getDailyFee() {
        return DAILY_FEE;
    }

    /**
     * 최대 연체료 조회
     */
    public Money getMaxOverdueFee() {
        return MAX_OVERDUE_FEE;
    }
}