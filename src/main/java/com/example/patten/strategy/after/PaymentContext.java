package com.example.patten.strategy.after;

public class PaymentContext {
    public void processPayment(PaymentStrategy strategy, int amount) {
        System.out.println(("결제 전략: " + strategy.getClass().getSimpleName()));
        System.out.println("결제를 시작합니다...");
        strategy.pay(amount);
        System.out.println("======================================");
    }
}
