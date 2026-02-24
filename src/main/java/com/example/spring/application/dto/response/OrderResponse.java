package com.example.spring.application.dto.response;

import com.example.spring.domain.model.Order;
import com.example.spring.domain.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private OrderStatus status;

    // 주문 항목
    private List<OrderItemResponse> orderItems;

    // 금액 정보
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    // 포인트 정보
    private Integer pointsUsed;
    private Integer pointsEarned;
    private String couponCode;

    // 결제 정보
    private PaymentResponse payment;

    // 배송 정보
    private DeliveryResponse delivery;

    // 날짜 정보
    private LocalDateTime orderDate;
    private LocalDateTime confirmedDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime cancelledDate;
    private String cancellationReason;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /**
     * Order 엔티티를 OrderResponse로 변환
     */
    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .memberId(order.getMember() != null ? order.getMember().getId() : null)
                .memberName(order.getMember() != null ? order.getMember().getName() : null)
                .memberEmail(order.getMember() != null ? order.getMember().getEmail() : null)
                .status(order.getStatus())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().getAmount() : null)
                .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount().getAmount() : null)
                .finalAmount(order.getFinalAmount() != null ? order.getFinalAmount().getAmount() : null)
                .pointsUsed(order.getPointsUsed())
                .pointsEarned(order.getPointsEarned())
                .couponCode(order.getCouponCode())
                .payment(PaymentResponse.from(order.getPayment()))
                .delivery(DeliveryResponse.from(order.getDelivery()))
                .orderDate(order.getOrderDate())
                .confirmedDate(order.getConfirmedDate())
                .shippedDate(order.getShippedDate())
                .deliveredDate(order.getDeliveredDate())
                .cancelledDate(order.getCancelledDate())
                .cancellationReason(order.getCancellationReason())
                .createdDate(order.getCreatedDate())
                .updatedDate(order.getUpdatedDate())
                .build();
    }
}