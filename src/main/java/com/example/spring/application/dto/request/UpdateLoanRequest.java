package com.example.spring.application.dto.request;

import com.example.spring.domain.model.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 대출 수정 요청 DTO
 * API 명세 #4: PATCH /api/admin/loans/{id}
 *
 * status 또는 dueDate 중 하나를 수정할 수 있습니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대출 수정 요청 (반납 처리 또는 날짜 연장)")
public class UpdateLoanRequest {

    /**
     * 대출 상태 (RETURNED로 변경 시 반납 처리)
     */
    @Schema(
            description = "대출 상태 (RETURNED로 변경 시 반납 처리)",
            example = "RETURNED",
            allowableValues = {"ACTIVE", "OVERDUE", "RETURNED", "CANCELLED"}
    )
    private LoanStatus status;

    /**
     * 반납 예정일 (연장 시 사용)
     */
    @Schema(
            description = "반납 예정일 (연장 시 사용, 현재 시간보다 미래여야 함)",
            example = "2025-10-30T11:45:00"
    )
    private LocalDateTime dueDate;
}