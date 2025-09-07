package com.example.patten.observer.after;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {OrderService.class, InventoryService.class, ShippingService.class})
public class ObserverPatternTest {

    @Autowired
    private OrderService orderService;

    @Test
    void springEventTest() {
        System.out.println("\n✨ Spring 이벤트(옵저버 패턴) 테스트 ✨");
        orderService.placeOrder("prod-456", "부산시 해운대구");
    }
}
