package com.example.spring.application.dto.response;

import com.example.spring.domain.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 도서 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private Boolean available;
    private String coverImageUrl;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /**
     * Book 엔티티를 BookResponse로 변환
     */
    public static BookResponse from(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn() != null ? book.getIsbn().getValue() : null)
                .price(book.getPrice() != null ? book.getPrice().getAmount() : null)
                .available(book.getAvailable())
                .coverImageUrl(book.getCoverImageUrl())
                .createdDate(book.getCreatedDate())
                .updatedDate(book.getUpdatedDate())
                .build();
    }
}