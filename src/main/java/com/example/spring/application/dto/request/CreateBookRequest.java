package com.example.spring.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 도서 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequest {

    @NotBlank(message = "{validation.book.title.required}")
    @Size(max = 200, message = "{validation.book.title.max200}")
    private String title;

    @NotBlank(message = "{validation.book.author.required}")
    @Size(max = 100, message = "{validation.book.author.max100}")
    private String author;

    @NotBlank(message = "{validation.book.isbn.required}")
    @Pattern(regexp = "^\\d{3}-?\\d{1,5}-?\\d{1,7}-?\\d{1,7}-?\\d{1}$|^\\d{13}$",
            message = "{validation.book.isbn.format}")
    private String isbn;

    @NotNull(message = "{validation.book.price.required}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{validation.book.price.positive}")
    @Digits(integer = 8, fraction = 2, message = "{validation.book.price.format}")
    private BigDecimal price;

    @Builder.Default
    private Boolean available = true;

    @Size(max = 500, message = "{validation.book.coverImageUrl.max500}")
    private String coverImageUrl;
}