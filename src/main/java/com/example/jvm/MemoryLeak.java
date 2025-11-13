package com.example.jvm;

import java.util.ArrayList;
import java.util.List;

// 문제 코드
public class MemoryLeak {
    private static List<String> list = new ArrayList<>();

    public static void main(String[] args) {
        while (true) {
            list.add(new String("데이터"));  // 계속 추가만 함!
        }
    }
}
