package com.example.patten.facade.before;

import org.junit.jupiter.api.Test;

public class ComplexClientTest {
    @Test
    void testOrderPlacement() {
        new OrderClient().placeOrder();
    }
}
