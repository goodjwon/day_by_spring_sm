package com.example.patten.strategy.before;

import org.junit.jupiter.api.Test;

public class PaymentServiceTest {
    @Test
    void testPayment() {
        PaymentService paymentService = new PaymentService();
        System.out.println("======= 신용카드 결제 테스트 =======");
        paymentService.processPayment("creditCard", 10000);
        System.out.println("\n======= 계좌이체 결제 테스트 =======");
        paymentService.processPayment("bankTransfer", 20000);
        System.out.println();
    }
}
