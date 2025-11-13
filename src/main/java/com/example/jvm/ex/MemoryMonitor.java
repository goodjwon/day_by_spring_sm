package com.example.jvm.ex;

import java.util.*;

public class MemoryMonitor {

    public static void printMemory() {
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory() / 1024 / 1024;
        long free = rt.freeMemory() / 1024 / 1024;
        long used = total - free;

        System.out.println("=== 메모리 상태 ===");
        System.out.println("전체: " + total + " MB");
        System.out.println("사용: " + used + " MB");
        System.out.println("여유: " + free + " MB");
        System.out.printf("사용률: %.1f%%\n",
                (double)used/total * 100);
    }

    public static void loadTest() {
        List<String> list = new ArrayList<>();

        System.out.println("\n10만 개 객체 생성 중...");
        for (int i = 0; i < 100000; i++) {
            list.add("데이터" + i);
        }

        printMemory();

        System.out.println("\n객체 제거 중...");
        list.clear();
        list = null;
        System.gc();

        try { Thread.sleep(1000); }
        catch (InterruptedException e) {}

        printMemory();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n1.메모리 확인  2.부하테스트  3.종료");
            System.out.print("선택: ");
            int choice = sc.nextInt();

            if (choice == 1) printMemory();
            else if (choice == 2) loadTest();
            else if (choice == 3) break;
        }

        sc.close();
    }
}
