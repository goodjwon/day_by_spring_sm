package com.example.spring.domain.model;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"createdDate", "updatedDate", "deletedDate"})
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{validation.book.title.required}")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "{validation.book.author.required}")
    @Column(nullable = false, length = 100)
    private String author;

    @NotNull(message = "{validation.book.isbn.required}")
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "isbn", nullable = false, unique = true, length = 17))
    private ISBN isbn;

    @NotNull(message = "{validation.book.price.required}")
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false, precision = 10, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency", length = 3))
    })
    private Money price;

    @Builder.Default
    @Column(nullable = false)
    private Boolean available = true;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

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

    /**
     * 도서 정보 수정
     */
    public void updateBookInfo(String title, String author, ISBN isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }

    /**
     * 가격 수정
     */
    public void updatePrice(Money newPrice) {
        this.price = newPrice;
    }

    public void updateCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    /**
     * 도서 사용 가능 여부 확인
     */
    public boolean isAvailable() {
        return available != null && available && !isDeleted();
    }

    /**
     * 대출 처리 (대출 불가능 상태로 변경)
     */
    public void loanOut() {
        this.available = false;
    }

    /**
     * 반납 처리 (대출 가능 상태로 변경)
     */
    public void returnBook() {
        this.available = true;
    }

    /**
     * 대출 가능 상태 설정 (명시적 메서드)
     */
    public void setAvailability(boolean available) {
        this.available = available;
    }
}