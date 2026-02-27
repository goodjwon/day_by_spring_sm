package com.example.spring;

import com.example.spring.application.dto.request.CreateOrderRequest;
import com.example.spring.application.dto.request.DeliveryRequest;
import com.example.spring.application.dto.request.OrderItemRequest;
import com.example.spring.application.dto.request.PaymentRequest;
import com.example.spring.domain.model.PaymentMethod;
import com.example.spring.application.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
class AopLoggingIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Test
    public void testAopLogging_ServiceCall() {
        System.out.println("\n=== AOP 로깅 테스트 시작 ===");

        CreateOrderRequest request = CreateOrderRequest.builder()
                .memberId(1L)
                .items(List.of(
                        OrderItemRequest.builder().bookId(1L).quantity(1).build()
                ))
                .payment(PaymentRequest.builder()
                        .method(PaymentMethod.CREDIT_CARD)
                        .amount(BigDecimal.TEN)
                        .build())
                .delivery(DeliveryRequest.builder()
                        .recipientName("Test")
                        .phoneNumber("010-0000-0000")
                        .address("Test Address")
                        .build())
                .build();

        try {
            orderService.createOrder(request);
        } catch (Exception e) {
            System.out.println("예상된 오류 (테스트 데이터 없음): " + e.getMessage());
        }

        System.out.println("=== AOP 로깅 테스트 완료 ===\n");
    }
}