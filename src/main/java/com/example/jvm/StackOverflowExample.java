package com.example.jvm;

// 잘못된 코드
public class StackOverflowExample {
    public static void recursion() {
        recursion();  // 자기 자신을 계속 호출!
    }

    public static void main(String[] args) {
        recursion();  // StackOverflowError 발생!
    }
}

