package com.example.spring.application;

public interface LoggingService {
    void log(String message);
    void error(String message, Exception e);
    void debug(String message);
}