package com.example.jvm;

import java.util.ArrayList;
import java.util.List;

public class MemoryTest {
    private static List<byte[]> memoryLeakList = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("MemoryTest 시작... VisualVM으로 연결하세요.");
        System.out.println("PID: " + ProcessHandle.current().pid());

        while (true) {
            byte[] b = new byte[1024 * 1024];
            memoryLeakList.add(b);

            System.out.println(memoryLeakList.size() + "MB 힙 사용 중...");

            Thread.sleep(1000);
        }
    }
}
