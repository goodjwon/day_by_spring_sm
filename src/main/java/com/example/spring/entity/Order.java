package com.example.spring.entity;

import com.example.spring.exception.OrderException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 주문 엔티티
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Delivery delivery;


    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;


    @Builder.Default
    private Integer pointsUsed = 0;

    @Builder.Default
    private Integer pointsEarned = 0;

    private String couponCode;

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
    protected  void onCreate(){
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected  void onUpdate(){
        updatedDate = LocalDateTime.now();
    }


    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public List<Book> getBooks() {
        return orderItems.stream()
                .map(OrderItem::getBook)
                .toList();
    }

    // 비즈니스 로직 메서드
    public void cancel(String reason){
        if(!isCancellable()){
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

    public BigDecimal getFinalAmount() {
        return totalAmount.subtract(discountAmount);
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
        if (payment != null) {
            payment.setOrder(this);
        }
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        if (delivery != null) {
            delivery.setOrder(this);
        }
    }









}