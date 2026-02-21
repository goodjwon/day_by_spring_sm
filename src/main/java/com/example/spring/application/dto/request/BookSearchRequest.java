package com.example.spring.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 도서 검색 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchRequest {

    private String title;

    private String author;

    private String keyword;

    @DecimalMin(value = "0.0", message = "{validation.search.price.min.nonNegative}")
    @Digits(integer = 8, fraction = 2, message = "{validation.book.price.format}")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", message = "{validation.search.price.max.nonNegative}")
    @Digits(integer = 8, fraction = 2, message = "{validation.book.price.format}")
    private BigDecimal maxPrice;

    private Boolean available;
}