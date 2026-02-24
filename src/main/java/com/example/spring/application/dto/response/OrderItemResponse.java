package com.example.spring.application.dto.response;

import com.example.spring.domain.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 주문 아이템 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;

    /**
     * OrderItem 엔티티를 OrderItemResponse로 변환
     */
    public static OrderItemResponse from(OrderItem orderItem) {
        BigDecimal priceAmount = orderItem.getPrice() != null ? orderItem.getPrice().getAmount() : BigDecimal.ZERO;
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .bookId(orderItem.getBook().getId())
                .bookTitle(orderItem.getBook().getTitle())
                .bookAuthor(orderItem.getBook().getAuthor())
                .bookIsbn(orderItem.getBook().getIsbn() != null ? orderItem.getBook().getIsbn().getValue() : null)
                .quantity(orderItem.getQuantity())
                .price(priceAmount)
                .totalPrice(priceAmount.multiply(new BigDecimal(orderItem.getQuantity())))
                .build();
    }
}