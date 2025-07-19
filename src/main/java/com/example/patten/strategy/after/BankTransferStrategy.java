package com.example.patten.strategy.after;

public class BankTransferStrategy implements PaymentStrategy{
    @Override
    public void pay(int amount) {
        System.out.println(" [성공] 계좌이체로 " + amount + "원 결제가 완료되었습니다.");
    }
}
