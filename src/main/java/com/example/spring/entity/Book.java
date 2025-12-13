package com.example.spring.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "book", indexes = {
        @Index(name = "idx_book_isbn", columnList = "isbn"),
        @Index(name = "idx_book_title", columnList = "title"),
        @Index(name = "idx_book_author", columnList = "author")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "도서 제목은 필수입니다")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "저자는 필수입니다")
    @Column(nullable = false, length = 100)
    private String author;

    @NotBlank(message = "ISBN은 필수입니다")
    @Pattern(
            regexp = "^\\d{3}-?\\d{1,5}-?\\d{1,7}-?\\d{1,7}-?\\d{1}$|^\\d{13}$|^ISBN\\d+$",
            message = "올바른 ISBN 형식이 아닙니다"
    )
    @Column(nullable = false, unique = true, length = 17)
    private String isbn;

    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(nullable = false)
    private Boolean available = true;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    public boolean isDeleted() {
        return deletedDate != null;
    }

    public void markAsDeleted() {
        this.deletedDate = LocalDateTime.now();
        this.available = false;
    }

    public void restore() {
        this.deletedDate = null;
        this.available = true;
    }
}