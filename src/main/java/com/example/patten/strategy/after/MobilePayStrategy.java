package com.example.patten.strategy.after;

public class MobilePayStrategy implements PaymentStrategy{
    @Override
    public void pay(int amount) {
        System.out.println(" [성공] 모바일 결재로 " + amount + "원 결제가 완료되었습니다.");
    }
}
