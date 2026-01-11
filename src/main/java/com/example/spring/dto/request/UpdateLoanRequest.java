package com.example.spring.dto.request;

import com.example.spring.entity.LoanStatus;
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
public class UpdateLoanRequest {

    /**
     * 대출 상태 (RETURNED로 변경 시 반납 처리)
     */
    private LoanStatus status;

    /**
     * 반납 예정일 (연장 시 사용)
     */
    private LocalDateTime dueDate;
}
