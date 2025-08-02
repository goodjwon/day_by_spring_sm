package com.example.patten.factory.before;

public interface Notifier {
    void send(String message);
}

class EmailNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("이메일 알림: " + message);
    }
}

class SmsNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("SMS 알림: " + message);
    }
}
