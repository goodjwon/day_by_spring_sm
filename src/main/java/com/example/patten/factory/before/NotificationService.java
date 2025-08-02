package com.example.patten.factory.before;

public class NotificationService {
    public static void sendNotification(String type, String message) {
        Notifier notifier;
        //클라이언트가 직접 객체를 생성하고 있음 (결합도 높음)
        if ("EMAIL".equalsIgnoreCase(type)) {
            notifier = new EmailNotifier();
        } else if ("SMS".equalsIgnoreCase(type)) {
            notifier = new SmsNotifier();
        } else {
            throw new IllegalArgumentException("알 수 없는 알림 타입입니다.");
        }
        System.out.println("[알림 전송] 생성된 객체: " + notifier.getClass().getSimpleName());
        notifier.send(message);
        System.out.println("------------------------------");
    }
}
