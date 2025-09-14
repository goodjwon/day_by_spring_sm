package com.example.spring.repository;

import com.example.spring.entity.Book;
import com.example.spring.entity.Member;
import com.example.spring.entity.MembershipType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    Optional<Book> findById(Long id);
    List<Book> findAll();
    Book save(Book book);
    void deleteById(Long id);

    // ISBN 관련 메서드
    Optional<Book> findByISBN(String isbn);
    boolean existsByIsbn(String isbn);

    // 제목/저자 검색 메서드
    List<Book> findByTitleContaining(String title);
    List<Book> findByAuthorContaining(String author);
    List<Book> findByTitleContainingOrAuthorContaining(String title, String author);

    //삭제되지 않은 도서 검색
    List<Book> findByAvailableTrue(boolean available);
    // Soft Delete 관련 메서드
    List<Book> findByDeletedDateIsNull();
    List<Book> findByDeletedDateIsNotNull();

    //가격 범위 검색
    List<Book> findByPriceBetween(BigDecimal lowPrice, BigDecimal highPrice);

    //편의 메서드 = Optional을 처리하고 않고 직접 Book 반환
    default Book findByIdOrThrow(Long id){
        return findById(id).orElse(null);
    }
}
