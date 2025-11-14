package com.example.jvm;

import java.util.ArrayList;
import java.util.List;

public class MemoryTest {

    // 객체를 담아둘 리스트 (메모리 누수 시뮬레이션)
    private static List<byte[]> memoryLeakList = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("MemoryTest 시작... VisualVM으로 연결하세요.");
        System.out.println("PID: " + ProcessHandle.current().pid());

        while (true) {
            // 1초마다 1MB 크기의 바이트 배열 객체를 생성
            byte[] b = new byte[1024 * 1024]; // 1MB
            memoryLeakList.add(b);

            System.out.println(memoryLeakList.size() + "MB 힙 사용 중...");

            // 너무 빠르지 않게 1초 대기
            Thread.sleep(1000);
        }
    }
}