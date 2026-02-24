package com.example.spring.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 주문 항목 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    @NotNull(message = "{validation.orderItem.bookId.required}")
    private Long bookId;

    @NotNull(message = "{validation.orderItem.quantity.required}")
    @Min(value = 1, message = "{validation.orderItem.quantity.min1}")
    private Integer quantity;
}