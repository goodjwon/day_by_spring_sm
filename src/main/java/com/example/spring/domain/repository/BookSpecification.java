package com.example.spring.domain.repository;

import com.example.spring.domain.model.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Book 동적 쿼리를 위한 Specification 클래스
 *
 * Specification 패턴을 사용하여 다양한 조건의 조합을 지원합니다.
 * 각 메서드는 단일 조건을 반환하며, and()/or()로 조합할 수 있습니다.
 */
public class BookSpecification {

    private BookSpecification() {
        // Utility class
    }

    /**
     * 복합 필터 조건 생성
     */
    public static Specification<Book> withFilters(String title, String author,
                                                  BigDecimal minPrice, BigDecimal maxPrice,
                                                  Boolean available) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 삭제되지 않은 도서만
            predicates.add(cb.isNull(root.get("deletedDate")));

            // 제목 검색
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%"
                ));
            }

            // 저자 검색
            if (author != null && !author.trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("author")),
                        "%" + author.toLowerCase() + "%"
                ));
            }

            // 최소 가격
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("price").get("amount"),
                        minPrice
                ));
            }

            // 최대 가격
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("price").get("amount"),
                        maxPrice
                ));
            }

            // 재고 상태
            if (available != null) {
                predicates.add(cb.equal(root.get("available"), available));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 제목 포함 검색
     */
    public static Specification<Book> titleContains(String title) {
        return (root, query, cb) -> {
            if (title == null || title.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"
            );
        };
    }

    /**
     * 저자 포함 검색
     */
    public static Specification<Book> authorContains(String author) {
        return (root, query, cb) -> {
            if (author == null || author.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("author")),
                    "%" + author.toLowerCase() + "%"
            );
        };
    }

    /**
     * 가격 범위 검색
     */
    public static Specification<Book> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("price").get("amount"),
                        minPrice
                ));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("price").get("amount"),
                        maxPrice
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 재고 상태 검색
     */
    public static Specification<Book> isAvailable(Boolean available) {
        return (root, query, cb) -> {
            if (available == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("available"), available);
        };
    }

    /**
     * 삭제되지 않은 도서만
     */
    public static Specification<Book> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedDate"));
    }

    /**
     * 삭제된 도서만
     */
    public static Specification<Book> deleted() {
        return (root, query, cb) -> cb.isNotNull(root.get("deletedDate"));
    }

    /**
     * 제목 또는 저자 검색 (키워드 검색)
     */
    public static Specification<Book> titleOrAuthorContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("author")), pattern)
            );
        };
    }
}