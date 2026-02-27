package com.example.spring.traditional;

import com.example.spring.domain.model.Order;

import java.util.List;

public class TraditionalBookstoreMain {
    public static void main(String[] args) {
        TraditionalOrderService orderService = new TraditionalOrderService();

        try {
            // 테스트용 주문 생성
            List<Long> bookIds = List.of(1L, 2L, 3L);

            System.out.println("=== 전통적인 Java 방식 실행 ===");
            Order order = orderService.createOrder(bookIds);

            System.out.println("주문 생성 성공!");
            System.out.println("주문 ID: " + order.getId());
            System.out.println("총 금액: " + order.getTotalAmount());
            System.out.println("주문 일시: " + order.getOrderDate());

        } catch (Exception e) {
            System.err.println("주문 생성 실패: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 자원 정리 - 개발자가 직접 해야 함
            orderService.cleanup();
        }
    }
}