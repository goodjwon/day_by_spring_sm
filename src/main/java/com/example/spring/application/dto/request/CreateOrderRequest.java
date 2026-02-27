package com.example.spring.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주문 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "{validation.order.memberId.required}")
    private Long memberId;

    @NotEmpty(message = "{validation.order.items.notEmpty}")
    @Valid
    private List<OrderItemRequest> items;

    // 할인 및 포인트 정보
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    private Integer pointsUsed = 0;

    private String couponCode;

    // 결제 정보
    @NotNull(message = "{validation.order.payment.required}")
    @Valid
    private PaymentRequest payment;

    // 배송 정보
    @NotNull(message = "{validation.order.delivery.required}")
    @Valid
    private DeliveryRequest delivery;
}