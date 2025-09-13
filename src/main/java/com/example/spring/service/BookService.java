package com.example.spring.service;

import com.example.spring.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BookService {

    /**
     * 도서 등록
     *
     * @param book 등록할 도서 정보
     * @return 등록된 도서
     * @throws IllegalArgumentException ISBN이 이미 존재하는 경우
     */
    Book createBook(Book book);


    /**
     * 도서 ID로 조회 (삭제된 도서 제외)
     *
     * @param id 도서 id
     * @return 도서 정보
     */
    Optional<Book> getBookById(Long id);

    /**
     * ISBN으로 도서 조회
     * @param isbn 도서 ISBN
     * @return 도서 정보
     */
    Optional<Book> getBookByIsbn(String isbn);

    /**
     * 모든 활성 도서 조회 (페이징)
     * @param pageable 페이징 정보
     * @return 도서 페이지
     */
    Page<Book> getAllActiveBooks(Pageable pageable);

    /**
     * 도서 정보 수정
     * @param id 도서 ID
     * @param book 수정할 도서 정보
     * @return 수정된 도서 정보
     * @throws IllegalArgumentException 도서를 찾을 수 없거나 ISBN이 중복되는 경우
     */
    Book updateBook(Long id, Book book);

    /**
     * 도서 삭제 (Soft Delete)
     * @param id 도서 ID
     * @throws IllegalArgumentException 도서를 찾을 수 없는 경우
     */
    void deleteBook(Long id);

    /**
     * 도서 복원
     * @param id 도서 ID
     * @throws IllegalArgumentException 도서를 찾을 수 없는 경우
     */
    void restoreBook(Long id);

    /**
     * 제목으로 도서 검색
     * @param title 검색할 제목
     * @return 검색된 도서 목록
     */
    List<Book> searchByTitle(String title);

    /**
     * 저자로 도서 검색
     * @param author 검색할 저자
     * @return 검색된 도서 목록
     */
    List<Book> searchByAuthor(String author);

    /**
     * 키워드로 도서 검색 (제목 또는 저자)
     * @param keyword 검색 키워드
     * @return 검색된 도서 목록
     */
    List<Book> searchByKeyword(String keyword);

    /**
     * 가격 범위로 도서 검색
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @return 검색된 도서 목록
     */
    List<Book> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 복합 조건으로 도서 검색
     * @param title 제목 (null 가능)
     * @param author 저자 (null 가능)
     * @param minPrice 최소 가격 (null 가능)
     * @param maxPrice 최대 가격 (null 가능)
     * @param available 재고 상태 (null 가능)
     * @param pageable 페이징 정보
     * @return 검색된 도서 페이지
     */
    Page<Book> searchBooksWithFilters(String title, String author,
                                      BigDecimal minPrice, BigDecimal maxPrice,
                                      Boolean available, Pageable pageable);

    /**
     * 재고 상태별 도서 조회
     * @param available 재고 상태
     * @return 도서 목록
     */
    List<Book> getBooksByAvailability(Boolean available);

    /**
     * ISBN 중복 검증
     * @param isbn 검증할 ISBN
     * @return 중복 여부
     */
    boolean isIsbnExists(String isbn);

    /**
     * 도서 재고 업데이트
     * @param id 도서 ID
     * @param available 재고 상태
     * @return 업데이트된 도서
     */
    Book updateBookAvailability(Long id, Boolean available);

    /**
     * 도서 통계 조회
     * @return 전체 도서 수
     */
    long getTotalBooksCount();

    /**
     * 활성 도서 통계 조회
     * @return 활성 도서 수
     */
    long getActiveBooksCount();

}