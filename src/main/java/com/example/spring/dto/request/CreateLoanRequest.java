package com.example.spring.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 대여 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLoanRequest {

    @NotNull(message = "회원 ID는 필수입니다")
    private Long memberId;

    @NotNull(message = "도서 ID는 필수입니다")
    private Long bookId;

    /**
     * 대여 기간 (일 단위, 기본값 14일)
     * 선택 가능한 값: 7, 14, 21, 30
     */
    @Builder.Default
    private Integer loanDays = 14;

    /**
     * 대여 메모 (선택사항)
     */
    private String notes;

    /**
     * 알림 받을 이메일 (선택사항, 미입력시 회원 이메일 사용)
     */
    private String notificationEmail;
}
