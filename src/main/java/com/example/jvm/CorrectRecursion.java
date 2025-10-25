package com.example.jvm;

public class CorrectRecursion {
    public static void countdown(int n) {
        if (n == 0) {
            return;  // 종료 조건 필수!
        }
        System.out.println(n);
        countdown(n - 1);
    }

    public static void main(String[] args) {
        countdown(10);  // 정상 동작
    }
}
