package com.example.spring.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * API 표준 에러 응답 포맷
 */
@Getter
@Builder
public class ErrorResponse {
    private final OffsetDateTime timestamp;
    private final int status;          // HTTP status code
    private final String error;        // HTTP status reason phrase
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String errorCode;    // 비즈니스 에러 코드 (예: DUPLICATE_EMAIL)
    private final String message;      // 사용자 메시지
    private final String path;         // 요청 경로

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<FieldError> fieldErrors; // 필드 검증 에러 목록

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String rejectedValue;
        private final String message;
    }
}