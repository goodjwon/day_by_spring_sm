package com.example.spring.presentation.controller;

import com.example.spring.application.dto.request.BookSearchRequest;
import com.example.spring.application.dto.request.CreateBookRequest;
import com.example.spring.application.dto.request.UpdateBookRequest;
import com.example.spring.application.dto.response.BookResponse;
import com.example.spring.domain.model.Book;
import com.example.spring.exception.BookException;
import com.example.spring.application.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 도서 관리 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 도서 등록
     */
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        log.info("도서 등록 요청 - ISBN: {}, 제목: {}", request.getIsbn(), request.getTitle());

        BookResponse response = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 도서 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBook(@PathVariable Long id) {
        log.debug("도서 조회 요청 - ID: {}", id);

        BookResponse response = bookService.getBookById(id)
                .orElseThrow(() -> new BookException.BookNotFoundException(id));
        return ResponseEntity.ok(response);
    }

    /**
     * 모든 활성 도서 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllActiveBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        log.debug("활성 도서 목록 조회 - page: {}, size: {}", page, size);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<Book> books = bookService.getAllActiveBooks(pageable);
        Page<BookResponse> response = books.map(BookResponse::from);

        return ResponseEntity.ok(response);
    }

    /**
     * 도서 정보 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request) {

        log.info("도서 수정 요청 - ID: {}", id);

        BookResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 도서 삭제 (Soft Delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("도서 삭제 요청 - ID: {}", id);

        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 도서 복원
     */
    @PatchMapping("/{id}/restore")
    public ResponseEntity<BookResponse> restoreBook(@PathVariable Long id) {
        log.info("도서 복원 요청 - ID: {}", id);

        bookService.restoreBook(id);
        BookResponse response = bookService.getBookById(id)
                .orElseThrow(() -> new BookException.BookNotFoundException(id));
        return ResponseEntity.ok(response);
    }

    /**
     * ISBN으로 도서 조회
     */
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookResponse> getBookByIsbn(@PathVariable String isbn) {
        log.debug("ISBN으로 도서 조회 - ISBN: {}", isbn);

        Book book = bookService.getBookByIsbn(isbn)
                .orElseThrow(() -> new BookException.BookNotFoundException("도서를 찾을 수 없습니다. ISBN: " + isbn));

        BookResponse response = BookResponse.from(book);
        return ResponseEntity.ok(response);
    }

    /**
     * 제목으로 도서 검색
     */
    @GetMapping("/search/title")
    public ResponseEntity<Page<BookResponse>> searchBooksByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("제목으로 도서 검색 - 키워드: {}", title);

        List<Book> books = bookService.searchByTitle(title);
        List<BookResponse> content = books.stream()
                .map(BookResponse::from)
                .toList();
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> response = toPage(content, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 저자로 도서 검색
     */
    @GetMapping("/search/author")
    public ResponseEntity<Page<BookResponse>> searchBooksByAuthor(
            @RequestParam String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("저자로 도서 검색 - 키워드: {}", author);

        List<Book> books = bookService.searchByAuthor(author);
        List<BookResponse> content = books.stream()
                .map(BookResponse::from)
                .toList();
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> response = toPage(content, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 키워드로 도서 검색
     */
    @GetMapping("/search/keyword")
    public ResponseEntity<Page<BookResponse>> searchBooksByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("키워드로 도서 검색 - 키워드: {}", keyword);

        List<Book> books = bookService.searchByKeyword(keyword);
        List<BookResponse> content = books.stream()
                .map(BookResponse::from)
                .toList();
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> response = toPage(content, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 가격 범위로 도서 검색
     */
    @GetMapping("/search/price")
    public ResponseEntity<Page<BookResponse>> searchBooksByPriceRange(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("가격 범위로 도서 검색 - 최소: {}, 최대: {}", minPrice, maxPrice);

        List<Book> books = bookService.searchByPriceRange(minPrice, maxPrice);
        List<BookResponse> content = books.stream()
                .map(BookResponse::from)
                .toList();
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> response = toPage(content, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 복합 조건으로 도서 검색 (페이징)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        log.debug("복합 조건으로 도서 검색 - title: {}, author: {}", title, author);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<Book> books = bookService.searchBooksWithFilters(
                title,
                author,
                minPrice,
                maxPrice,
                available,
                pageable
        );

        Page<BookResponse> response = books.map(BookResponse::from);
        return ResponseEntity.ok(response);
    }

    /**
     * 복합 조건으로 도서 검색 (JPQL Query 버전)
     */
    @GetMapping("/search/query")
    public ResponseEntity<Page<BookResponse>> searchBooksWithQueryFilters(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        log.debug("복합 조건으로 도서 검색 (Query 버전) - title: {}, author: {}", title, author);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<Book> books = bookService.searchBooksWithQueryFilters(
                title,
                author,
                minPrice,
                maxPrice,
                available,
                pageable
        );

        Page<BookResponse> response = books.map(BookResponse::from);
        return ResponseEntity.ok(response);
    }

    /**
     * 재고 상태별 도서 조회
     */
    @GetMapping("/availability/{available}")
    public ResponseEntity<List<BookResponse>> getBooksByAvailability(
            @PathVariable Boolean available) {

        log.debug("재고 상태별 도서 조회 - 재고: {}", available);

        List<Book> books = bookService.getBooksByAvailability(available);
        List<BookResponse> response = books.stream()
                .map(BookResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * 도서 재고 상태 업데이트
     */
    @PatchMapping("/{id}/availability")
    public ResponseEntity<BookResponse> updateBookAvailability(
            @PathVariable Long id,
            @RequestParam Boolean available) {

        log.info("도서 재고 상태 업데이트 - ID: {}, 재고: {}", id, available);

        Book book = bookService.updateBookAvailability(id, available);
        BookResponse response = BookResponse.from(book);

        return ResponseEntity.ok(response);
    }

    /**
     * ISBN 중복 확인
     */
    @GetMapping("/validate/isbn")
    public ResponseEntity<Boolean> validateIsbn(@RequestParam String isbn) {
        log.debug("ISBN 중복 확인 - ISBN: {}", isbn);

        boolean exists = bookService.isIsbnExists(isbn);
        return ResponseEntity.ok(exists);
    }

    /**
     * 도서 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<BookStatistics> getBookStatistics() {
        log.debug("도서 통계 조회");

        long totalBooks = bookService.getTotalBooksCount();
        long activeBooks = bookService.getActiveBooksCount();

        BookStatistics statistics = BookStatistics.builder()
                .totalBooks(totalBooks)
                .activeBooks(activeBooks)
                .deletedBooks(totalBooks - activeBooks)
                .build();

        return ResponseEntity.ok(statistics);
    }

    /**
     * 도서 통계 응답 DTO
     */
    @lombok.Getter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BookStatistics {
        private long totalBooks;
        private long activeBooks;
        private long deletedBooks;
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> pageContent = start >= items.size() ? List.of() : items.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, items.size());
    }
}