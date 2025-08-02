package com.example.patten.factory.after;

public class EmailNotifierFactory implements NotifierFactory {
    @Override
    public Notifier createNotifier() {
        System.out.println(">> Email 공장(Factory)에서 'EmailNotifier'를 생성합니다.");
        return new EmailNotifier();
    }
}
