package com.example.patten.observer.before;

import org.junit.jupiter.api.Test;

public class TightlyCoupledServiceTest {
    @Test
    void testOrderPlacement() {
        OrderService orderService = new OrderService();
        orderService.placeOrder("prod-123", "서울시 강남구");
    }
}
