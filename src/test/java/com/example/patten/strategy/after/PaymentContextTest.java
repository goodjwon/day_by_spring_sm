package com.example.patten.strategy.after;

import org.junit.jupiter.api.Test;

public class PaymentContextTest {
    @Test
    void strategyPatternTest() {
        PaymentContext context = new PaymentContext();

        System.out.println("✨ 전략 패턴 적용 후 코드 ✨");
        System.out.println("--------------------------------------");

        // 첫 번째 전략: 신용카드
        PaymentStrategy creditCard = new CreditCardStrategy();
        context.processPayment(creditCard, 15000);

        // 두 번째 전략: 계좌이체 (컨텍스트 코드 변경 없이 전략만 교체)
        PaymentStrategy bankTransfer = new BankTransferStrategy();
        context.processPayment(bankTransfer, 30000);

        // 향후 모바일 결제가 추가된다면?
        // MobilePayStrategy 클래스만 추가하고 아래처럼 사용하면 끝!
         PaymentStrategy mobilePay = new MobilePayStrategy();
         context.processPayment(mobilePay, 5000);
    }
}
