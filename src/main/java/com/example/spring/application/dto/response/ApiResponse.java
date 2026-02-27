package com.example.spring.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 표준 API 응답 래퍼
 * 성공 메시지와 데이터를 함께 반환할 때 사용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 성공 메시지
     */
    private String message;

    /**
     * 응답 데이터
     */
    private T data;

    /**
     * 추가 알림 메시지 (선택적)
     */
    private String notification;

    /**
     * 성공 응답 생성 (데이터만)
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .build();
    }

    /**
     * 성공 응답 생성 (메시지 + 데이터)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 성공 응답 생성 (메시지 + 데이터 + 알림)
     */
    public static <T> ApiResponse<T> success(String message, T data, String notification) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .notification(notification)
                .build();
    }

    /**
     * 성공 응답 생성 (메시지만)
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .build();
    }
}