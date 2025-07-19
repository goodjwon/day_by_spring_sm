package com.example.patten.strategy.after;

public class CreditCardStrategy implements PaymentStrategy{
    @Override
    public void pay(int amount) {
        System.out.println(" [성공] 신용카드로 " + amount + "원 결제가 완료되었습니다.");
    }
}
