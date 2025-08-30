package com.example.spring.dto.response;

import com.example.spring.exception.GlobalExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {
    private String errorCode; // "DUPLICATE_EMAIL", "MEMBER_NOT_FOUND"
    private String message; // 사용자 친화적 메시지
    private LocalDateTime timestamp; // 오류 발생 시각
    private List<GlobalExceptionHandler.FieldErrorDetail> fieldErrors; // Bean Validation 상세 오류
}
