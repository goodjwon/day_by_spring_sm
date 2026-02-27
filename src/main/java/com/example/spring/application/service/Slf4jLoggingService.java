package com.example.spring.application.service;

import com.example.spring.application.LoggingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Slf4jLoggingService implements LoggingService {

    @Override
    public void log(String message) {
        log.info(message);
    }

    @Override
    public void error(String message, Exception e) {
        log.error(message, e);
    }

    @Override
    public void debug(String message) {
        log.debug(message);
    }
}