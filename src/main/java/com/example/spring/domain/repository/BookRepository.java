package com.example.spring.domain.repository;

import com.example.spring.domain.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Book Repository - Spring Data JPA 기반
 *
 * JpaRepository: 기본 CRUD 제공
 * JpaSpecificationExecutor: 동적 쿼리 (Specification 패턴) 지원
 */
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    // ========== ISBN 관련 메서드 ==========

    @Query("SELECT b FROM Book b WHERE b.isbn.value = :isbn")
    Optional<Book> findByIsbnValue(@Param("isbn") String isbn);

    @Query("SELECT (COUNT(b) > 0) FROM Book b WHERE b.isbn.value = :isbn")
    boolean existsByIsbnValue(@Param("isbn") String isbn);

    // ========== 제목/저자 검색 메서드 ==========

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.title")
    List<Book> findByTitleContainingOrAuthorContaining(@Param("keyword") String keyword);

    // ========== 가격 범위 검색 ==========

    @Query("SELECT b FROM Book b WHERE b.price.amount BETWEEN :minPrice AND :maxPrice ORDER BY b.price.amount")
    List<Book> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    // ========== 재고 상태별 조회 ==========

    List<Book> findByAvailable(Boolean available);

    // ========== 복합 조건 검색 ==========

    @Query("SELECT b FROM Book b WHERE b.available = :available AND LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Book> findByAvailableAndTitleContaining(@Param("available") Boolean available, @Param("title") String title);

    @Query("SELECT b FROM Book b WHERE b.available = :available AND LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
    List<Book> findByAvailableAndAuthorContaining(@Param("available") Boolean available, @Param("author") String author);

    @Query("SELECT b FROM Book b WHERE b.available = :available AND b.price.amount BETWEEN :minPrice AND :maxPrice ORDER BY b.price.amount")
    List<Book> findByAvailableAndPriceBetween(@Param("available") Boolean available,
                                              @Param("minPrice") BigDecimal minPrice,
                                              @Param("maxPrice") BigDecimal maxPrice);

    // ========== 날짜 범위 검색 ==========

    List<Book> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // ========== Soft Delete 관련 메서드 ==========

    List<Book> findByDeletedDateIsNull();

    List<Book> findByDeletedDateIsNotNull();

    List<Book> findByDeletedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // ========== 통계 ==========

    long countByAvailable(Boolean available);

    long countByDeletedDateIsNull();

    // ========== 편의 메서드 ==========

    default Book findBookById(Long id) {
        return findById(id).orElse(null);
    }

    default List<Book> findActiveBooks() {
        return findByDeletedDateIsNull();
    }

    default List<Book> searchBooks(String keyword) {
        return findByTitleContainingOrAuthorContaining(keyword);
    }

    /**
     * ISBN으로 도서 조회 (ISBN VO 정규화 적용)
     */
    default Optional<Book> findByIsbn(String isbn) {
        // ISBN을 정규화하여 조회
        String normalizedIsbn = com.example.spring.domain.vo.ISBN.of(isbn).getValue();
        return findByIsbnValue(normalizedIsbn);
    }

    /**
     * ISBN 존재 여부 확인 (ISBN VO 정규화 적용)
     */
    default boolean existsByIsbn(String isbn) {
        String normalizedIsbn = com.example.spring.domain.vo.ISBN.of(isbn).getValue();
        return existsByIsbnValue(normalizedIsbn);
    }
}