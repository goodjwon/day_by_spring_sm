package com.example.spring.entity;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
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

    @NotNull(message = "ISBN은 필수입니다")
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "isbn",
            nullable = false, unique = true, length = 17))
    private ISBN isbn;

    @NotNull(message = "가격은 필수입니다")
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "price",
            nullable = false, precision = 10, scale = 2))
    private Money price;

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


    @PrePersist
    protected void onCreate(){
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedDate = LocalDateTime.now();
    }

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

    public void setAvailability(boolean available) {
        this.available = available;
    }

    public void loanOut() {
        this.available = false;
    }
}