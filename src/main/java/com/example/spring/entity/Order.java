package com.example.spring.entity;

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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "final_amount", nullable = false)
    private BigDecimal finalAmount;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "points_used")
    private BigDecimal pointsUsed;

    @Column(name = "points_earned")
    private BigDecimal pointsEarned;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "confirmed_note")
    private boolean confirmedDate;

    @Column(name = "shipped_note")
    private boolean shippedDate;

    @Column(name = "delivered_note")
    private boolean deliveredDate;

    @Column(name = "cancelled_note")
    private boolean cancelledDate;

    @Column(name = "cancellation_reason")
    private boolean cancellationReason;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    private List<Payment> payment = new ArrayList<>();

    private List<Delivery> delivery = new ArrayList<>();


    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public void addPayment(Payment payment) {
        this.payment.add(payment);
    }

    public void addDelivery(Delivery delivery) {
        this.delivery.add(delivery);
    }

    public List<Book> getBooks() {
        return orderItems.stream()
                .map(OrderItem::getBook)
                .toList();
    }
}