package com.example.spring.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 도서 수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookRequest {

    @NotBlank(message = "도서 제목은 필수입니다")
    @Size(max = 200, message = "도서 제목은 200자를 초과할 수 없습니다")
    private String title;

    @NotBlank(message = "저자는 필수입니다")
    @Size(max = 100, message = "저자는 100자를 초과할 수 없습니다")
    private String author;

    @NotBlank(message = "ISBN은 필수입니다")
    @Pattern(regexp = "^\\d{3}-?\\d{1,5}-?\\d{1,7}-?\\d{1,7}-?\\d{1}$|^\\d{13}$",
            message = "올바른 ISBN 형식이 아닙니다")
    private String isbn;

    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다")
    @Digits(integer = 8, fraction = 2, message = "가격 형식이 올바르지 않습니다")
    private BigDecimal price;

    @NotNull(message = "재고 상태는 필수입니다")
    private Boolean available;
}