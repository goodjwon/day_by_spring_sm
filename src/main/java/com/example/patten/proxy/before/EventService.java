package com.example.patten.proxy.before;

public class EventService {
    public void processEvent(String eventName) {
        long startTime = System.currentTimeMillis();

        System.out.println("이벤트 처리 시작: " + eventName);
        try {
            Thread.sleep(1000);
        }  catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("이벤트 처리 완료.");

        long endTime = System.currentTimeMillis();
        System.out.println("== 실행 시간: " + (endTime - startTime));
    }
}
