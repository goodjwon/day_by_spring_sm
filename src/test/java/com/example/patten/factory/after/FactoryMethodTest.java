package com.example.patten.factory.after;

import org.junit.jupiter.api.Test;

public class FactoryMethodTest {
    @Test
    void factoryMethodPatternTest() {
        System.out.println("✨ 팩토리 메서드 패턴 적용 후 코드 ✨\n");

        Client client = new Client();

        // 이메일 알림이 필요할 땐 Email 공장을 사용
        NotifierFactory emailFactory = new EmailNotifierFactory();
        client.send(emailFactory, "주문이 완료되었습니다.");

        // SMS 알림이 필요할 땐 SMS 공장을 사용
        NotifierFactory smsFactory = new SmsNotifierFactory();
        client.send(smsFactory, "배송이 시작되었습니다.");

        // 만약 Push 알림이 추가된다면?
        // PushNotifier와 PushNotifierFactory 클래스만 추가하고
        // 아래처럼 사용하면 끝! 클라이언트 코드는 변경되지 않습니다.
        // NotifierFactory pushFactory = new PushNotifierFactory();
        // client.send(pushFactory, "이벤트에 당첨되셨습니다!");
    }
}
