package com.example.patten.proxy.after;

import org.springframework.stereotype.Service;

@Service
public class EventService {
    public void processEvent(String eventName) {
        // 오직 순수한 비즈니스 로직만 남음
        System.out.println("이벤트 처리 시작: " + eventName);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("이벤트 처리 완료.");
    }
}
