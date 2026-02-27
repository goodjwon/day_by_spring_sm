package com.example.spring.domain.service;

import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 주문 금액 계산 도메인 서비스
 *
 * 주문 금액 계산 로직을 중앙화합니다.
 * 할인 정책, 배송비 계산 등을 일관되게 적용합니다.
 */
@Component
public class OrderAmountCalculator {

    /**
     * 무료 배송 기준 금액
     */
    private static final Money FREE_SHIPPING_THRESHOLD = Money.of(30000);

    /**
     * 기본 배송비
     */
    private static final Money DEFAULT_SHIPPING_FEE = Money.of(3000);

    /**
     * 포인트 적립률 (1%)
     */
    private static final double POINTS_RATE = 0.01;

    /**
     * 주문 항목들의 총 금액 계산
     *
     * @param items 주문 항목 목록
     * @return 총 금액
     */
    public Money calculateTotal(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return Money.zero();
        }

        return items.stream()
                .map(this::calculateItemTotal)
                .reduce(Money.zero(), Money::add);
    }

    /**
     * 단일 주문 항목의 금액 계산
     *
     * @param item 주문 항목
     * @return 항목 금액 (가격 x 수량)
     */
    public Money calculateItemTotal(OrderItem item) {
        return item.getPrice().multiply(item.getQuantity());
    }

    /**
     * 할인 적용
     *
     * @param total 총 금액
     * @param discount 할인 금액
     * @return 할인 적용 후 금액
     */
    public Money applyDiscount(Money total, Money discount) {
        if (discount == null || discount.isZero()) {
            return total;
        }

        Money result = total.subtract(discount);

        // 할인 후 금액이 0 미만이 되지 않도록 보장
        return result.isNegative() ? Money.zero() : result;
    }

    /**
     * 퍼센트 할인 적용
     *
     * @param total 총 금액
     * @param discountPercent 할인율 (0-100)
     * @return 할인 적용 후 금액
     */
    public Money applyPercentDiscount(Money total, int discountPercent) {
        if (discountPercent <= 0) {
            return total;
        }
        if (discountPercent >= 100) {
            return Money.zero();
        }

        Money discountAmount = total.multiply(discountPercent).divide(100);
        return total.subtract(discountAmount);
    }

    /**
     * 배송비 계산
     *
     * @param orderTotal 주문 총 금액
     * @return 배송비
     */
    public Money calculateShippingFee(Money orderTotal) {
        // 무료 배송 기준 금액 이상이면 배송비 무료
        if (orderTotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return Money.zero();
        }
        return DEFAULT_SHIPPING_FEE;
    }

    /**
     * 최종 결제 금액 계산 (상품 금액 + 배송비 - 할인)
     *
     * @param items 주문 항목 목록
     * @param discount 할인 금액
     * @return 최종 결제 금액
     */
    public Money calculateFinalAmount(List<OrderItem> items, Money discount) {
        Money subtotal = calculateTotal(items);
        Money discountedTotal = applyDiscount(subtotal, discount);
        Money shippingFee = calculateShippingFee(subtotal);

        return discountedTotal.add(shippingFee);
    }

    /**
     * 적립 예정 포인트 계산
     *
     * @param paymentAmount 결제 금액
     * @return 적립 예정 포인트
     */
    public int calculatePointsToEarn(Money paymentAmount) {
        return (int) (paymentAmount.getAmount().doubleValue() * POINTS_RATE);
    }

    /**
     * 포인트 사용 적용
     *
     * @param total 총 금액
     * @param points 사용할 포인트
     * @return 포인트 사용 후 금액
     */
    public Money applyPoints(Money total, int points) {
        if (points <= 0) {
            return total;
        }

        Money pointDiscount = Money.of(points);
        Money result = total.subtract(pointDiscount);

        // 포인트 사용 후 금액이 0 미만이 되지 않도록 보장
        return result.isNegative() ? Money.zero() : result;
    }

    /**
     * 무료 배송 기준 금액 조회
     */
    public Money getFreeShippingThreshold() {
        return FREE_SHIPPING_THRESHOLD;
    }

    /**
     * 기본 배송비 조회
     */
    public Money getDefaultShippingFee() {
        return DEFAULT_SHIPPING_FEE;
    }

    /**
     * 포인트 적립률 조회
     */
    public double getPointsRate() {
        return POINTS_RATE;
    }
}