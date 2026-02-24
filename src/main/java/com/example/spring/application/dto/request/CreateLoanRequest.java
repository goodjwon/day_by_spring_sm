package com.example.spring.application.dto.request;

import com.example.spring.validation.ValidLoanPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대여 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "도서 대여 생성 요청")
public class CreateLoanRequest {

    @NotNull(message = "{validation.loan.memberId.required}")
    @Schema(description = "회원 ID", example = "1", required = true)
    private Long memberId;

    @NotNull(message = "{validation.loan.bookId.required}")
    @Schema(description = "도서 ID", example = "1", required = true)
    private Long bookId;

    /**
     * 대여 기간 (일 단위, 기본값 14일)
     * 선택 가능한 값: 7, 14, 21, 30
     */
    @ValidLoanPeriod
    @Builder.Default
    @Schema(
            description = "대여 기간 (일) - 셀렉트 박스 선택",
            example = "14",
            defaultValue = "14",
            allowableValues = {"7", "14", "21", "30"}
    )
    private Integer loanDays = 14;

    /**
     * 대여 메모 (선택사항)
     */
    @Schema(description = "대여 메모", example = "학기말 과제용", required = false)
    private String notes;

    /**
     * 알림 받을 이메일 (선택사항, 미입력시 회원 이메일 사용)
     */
    @Schema(description = "알림 이메일", example = "custom@email.com", required = false)
    private String notificationEmail;
}