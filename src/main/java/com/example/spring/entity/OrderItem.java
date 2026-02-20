package com.example.spring.entity;

import com.example.spring.domain.vo.Money;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// 주문 상품 엔티티
@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Money price;

    private Money totalPrice;

    public OrderItem(Integer quantity, Money price, Money totalPrice) {
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
    }

    public void changeQuantity(Integer quantity) {
        this.quantity = quantity;
        totalPrice = price.multiply(quantity);
    }

    public void updatePrice(Money price) {
        this.price = price;
        totalPrice = price.multiply(quantity);
    }

    public Money getTotalPrice() {
        return totalPrice = price.multiply(quantity);
    }
}