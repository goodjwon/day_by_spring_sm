package com.example.patten.factory.before;

import org.junit.jupiter.api.Test;

public class NotificationServiceTest {
    @Test
    void testNotification() {
        NotificationService notificationService = new NotificationService();
        notificationService.sendNotification("EMAIL", "회원가입을 축하합니다!");
        notificationService.sendNotification("SMS", "인증번호 [1234]를 입력해주세요.");
    }
}
