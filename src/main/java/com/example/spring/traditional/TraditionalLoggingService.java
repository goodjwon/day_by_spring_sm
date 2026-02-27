package com.example.spring.traditional;

import java.time.LocalDateTime;

// 3. 로깅 서비스 - 단순한 구현
public class TraditionalLoggingService {
    public void log(String message) {
        System.out.println("[LOG] " + LocalDateTime.now() + ": " + message);
    }

    public void error(String message, Exception e) {
        System.err.println("[ERROR] " + LocalDateTime.now() + ": " + message);
        if (e != null) {
            e.printStackTrace();
        }
    }
}