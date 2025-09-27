package com.example.spring.dto.response;

import com.example.spring.entity.Book;
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
                .isbn(book.getIsbn())
                .price(book.getPrice())
                .available(book.getAvailable())
                .createdDate(book.getCreatedDate())
                .updatedDate(book.getUpdatedDate())
                .build();
    }
}