package com.example.patten.factory.after;

public class SmsNotifierFactory implements NotifierFactory {
    @Override
    public Notifier createNotifier() {
        System.out.println(">> SMS 공장(Factory)에서 'SmsNotifier'를 생성합니다.");
        return new SmsNotifier();
    }
}
