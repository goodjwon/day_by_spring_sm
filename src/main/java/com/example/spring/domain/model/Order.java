package com.example.spring.domain.model;

import com.example.spring.domain.vo.Money;
import com.example.spring.exception.OrderException;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 주문 엔티티
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_member_id", columnList = "member_id"),
        @Index(name = "idx_order_date", columnList = "order_date"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_created_date", columnList = "created_date")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문자 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    private List<OrderItem> orderItems = new ArrayList<>();

    // 결제 정보 (1:1 관계)
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    // 배송 정보 (1:1 관계)
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Delivery delivery;

    // 금액 정보
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "total_currency", length = 3))
    })
    private Money totalAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "discount_amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "discount_currency", length = 3))
    })
    @Builder.Default
    private Money discountAmount = Money.zero();

    // 포인트 정보
    @Builder.Default
    private Integer pointsUsed = 0;

    @Builder.Default
    private Integer pointsEarned = 0;

    // 쿠폰 정보
    private String couponCode;

    // 주문 상태 및 날짜
    @Column(nullable = false)
    private LocalDateTime orderDate;

    private LocalDateTime confirmedDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    // 취소 정보
    private LocalDateTime cancelledDate;
    private String cancellationReason;

    // 생성 및 수정 일시
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.attachToOrder(this);
    }

    public List<Book> getBooks() {
        return orderItems.stream()
                .map(OrderItem::getBook)
                .toList();
    }

    // 비즈니스 로직 메서드
    public void cancel(String reason) {
        if (!isCancellable()) {
            throw new OrderException.OrderCancellationNotAllowedException(this.id, this.status);
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledDate = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public boolean isCancellable() {
        // PENDING, CONFIRMED 상태에서만 취소 가능
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.CONFIRMED;
    }

    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new OrderException.InvalidOrderStateException("확인할 수 없는 주문입니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.CONFIRMED;
        this.confirmedDate = LocalDateTime.now();
    }

    public void ship() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new OrderException.InvalidOrderStateException("배송을 시작할 수 없는 주문입니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.SHIPPED;
        this.shippedDate = LocalDateTime.now();
    }

    public void deliver() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new OrderException.InvalidOrderStateException("배송완료 처리할 수 없는 주문입니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.DELIVERED;
        this.deliveredDate = LocalDateTime.now();
    }

    public Money getFinalAmount() {
        return totalAmount.subtract(discountAmount);
    }

    /**
     * 결제 정보 연결
     */
    public void attachPayment(Payment payment) {
        this.payment = payment;
        if (payment != null) {
            payment.attachToOrder(this);
        }
    }

    /**
     * 배송 정보 연결
     */
    public void attachDelivery(Delivery delivery) {
        this.delivery = delivery;
        if (delivery != null) {
            delivery.attachToOrder(this);
        }
    }

    /**
     * 할인 적용
     */
    public void applyDiscount(Money discount) {
        this.discountAmount = discount;
    }

    /**
     * 포인트 사용
     */
    public void usePoints(Integer points) {
        this.pointsUsed = points;
    }

    /**
     * 포인트 적립
     */
    public void earnPoints(Integer points) {
        this.pointsEarned = points;
    }

    /**
     * 쿠폰 적용
     */
    public void applyCoupon(String couponCode) {
        this.couponCode = couponCode;
    }

    /**
     * 주문 총액 설정 (내부용)
     */
    void updateTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }
}