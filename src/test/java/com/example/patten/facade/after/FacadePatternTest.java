package com.example.patten.facade.after;

import org.junit.jupiter.api.Test;

public class FacadePatternTest {
    @Test
    void facadeTest() {
        System.out.println("\\n✨ 파사드 패턴 적용 후 테스트 ✨");
        new OrderClient().placeOrder();
    }
}
