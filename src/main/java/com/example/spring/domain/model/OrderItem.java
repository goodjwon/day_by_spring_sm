package com.example.spring.domain.model;

import com.example.spring.domain.vo.Money;
import com.example.spring.exception.ErrorMessages;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// 주문 상품 엔티티
@Entity
@Table(name = "order_item")
@Getter
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

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency", length = 3))
    })
    private Money price;

    public Money getTotalPrice() {
        return price.multiply(quantity);
    }

    /**
     * 주문에 연결 (package-private, Order.addOrderItem()에서 사용)
     */
    void attachToOrder(Order order) {
        this.order = order;
    }

    /**
     * 수량 변경
     */
    public void changeQuantity(Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException(ErrorMessages.ORDER_ITEM_QUANTITY_MIN_ONE);
        }
        this.quantity = newQuantity;
    }

    /**
     * 가격 설정 (주문 생성 시)
     */
    public void updatePrice(Money newPrice) {
        this.price = newPrice;
    }

    /**
     * 도서 설정 (주문 생성 시)
     */
    void assignBook(Book book) {
        this.book = book;
    }
}