package com.example.spring.controller;

import com.example.spring.dto.request.CreateBookRequest;
import com.example.spring.dto.request.UpdateBookRequest;
import com.example.spring.dto.response.BookResponse;
import com.example.spring.entity.Book;
import com.example.spring.service.BookService;
import jakarta.validation.Valid;
import lombok.Getter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    // 도서 등록 API (POST /api/books)
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        log.info("도서 등록 요청 - 도서명: {}", request.getTitle());
        BookResponse response = bookService.createBook(request);
        log.info("도서 등록 성공 - ID: {}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 도서 단건 조회 API (GET /api/books/{id})
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBook(@PathVariable Long id) {
        log.info("도서 조회 요청 - ID: {}", id);
        BookResponse response = bookService.getBookById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 도서 목록 조회 API (GET /api/books)
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
     * 도서 정보 수정 (PUT /api/books/{id})
     */
    @PutMapping("/api/books/{id}")
    public ResponseEntity<BookResponse> updateBook(Long id, @Valid @RequestBody UpdateBookRequest request) {
        log.info("도서 정보 수정 요청 - 도서명: {}", request.getTitle());

        BookResponse response = bookService.updateBook(id,request);

        log.info("도서 정보 수정 요청  - ID: {}", response.getId());

        return ResponseEntity.ok(response);
    }

     /**
     * 도서 삭제 (DELETE /api/books/{id}) - Soft Delete
     */
     @DeleteMapping("/{id}")
     public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
         log.info("도서 삭제 요청 - ID: {}", id);

         bookService.deleteBook(id);

         return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
     }

    /**
     * 도서 복원 @PatchMapping("/{id}/restore")
     */
    @PatchMapping("/{id}/restore")
    public ResponseEntity<BookResponse> restoreBook(@PathVariable Long id) {
        log.info("도서 복원 요청 - ID: {}", id);

        bookService.restoreBook(id);
        BookResponse response = bookService.getBookById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * ISBN으로 도서 조회 @GetMapping("/isbn/{isbn}")
     */
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookResponse> getBookByISBN(String isbn) {
        log.info("ISBN으로 도서 조회 요청 - ISBN: {}",  isbn);

        Optional<Book> bookByIsbn = bookService.getBookByIsbn(isbn);

        return ResponseEntity.status(HttpStatus.OK).body(BookResponse.from(bookByIsbn.get()));
    }

    /**
     * 제목으로 도서 검색 @GetMapping("/search/title")
     */
    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> searchByTitle(CreateBookRequest request) {
        log.info("제목으로 도서 조회 요청 -  Title: {}",  request.getTitle());

        List<Book> searchBook = bookService.searchByTitle(request.getTitle());

        return ResponseEntity.ok().body(searchBook);
    }

    /**
     * 저자로 도서 검색 @GetMapping("/search/author")
     */
    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> searchByAuthor(CreateBookRequest request) {
        log.info("저자로 도서 조회 요청  -  Author: {}",  request.getAuthor());

        List<Book> searchBook = bookService.searchByAuthor(request.getAuthor());

        return ResponseEntity.ok().body(searchBook);
    }

    /**
     * 키워드로 도서 검색 @GetMapping("/search/keyword")
     */
    @GetMapping("search/keyword")
    public ResponseEntity<List<Book>> searchByKeyword(String keyword) {
        log.info("키워드로 도서 조회 요청  -  Keyword: {}", keyword);
        if (keyword == null) {
            System.out.println("키워드를 입력해 주세요");
        }
        List<Book> searchBook = bookService.searchByKeyword(keyword);

        return ResponseEntity.ok().body(searchBook);
    }

    /**
     * 가격 범위로 도서 검색 @GetMapping("/search/price")
     */
    @GetMapping("/search/price")
    public ResponseEntity<List<Book>> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("가격 범위로 도서 조회 요청  -  minPrice: {}, maxPrice: {}", maxPrice, minPrice);
        if (minPrice == null || maxPrice == null) {
            System.out.println("검색할 도서의 가격 범위를 입력해 주세요");
        }
        List<Book> searchBook = bookService.searchByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok().body(searchBook);
    }

    /**
     * 복합 조건으로 도서 검색 (페이징) @GetMapping("/search")
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBooksWithFilters(String title, String author,
                                                             BigDecimal minPrice, BigDecimal maxPrice,
                                                             Boolean available, Pageable pageable) {
        log.info("복합 조건으로 도서 조회 요청");
        pageable = PageRequest.of(0,10, Sort.Direction.DESC);
        Page<Book> result = bookService.searchBooksWithFilters(title, author, minPrice, maxPrice, available, pageable);


        return ResponseEntity.ok().body(result);
    }

    /**
     * 재고 상태별 도서 조회 @GetMapping("/availability/{available}")
     */
    @GetMapping("/availability/{available}")
    public ResponseEntity<List<Book>> getBooksByAvailability(@PathVariable Boolean available) {
        log.info("재고 상태별 도서 조회 요청");

        List<Book> searchBook = bookService.getBooksByAvailability(available);

        return ResponseEntity.status(HttpStatus.OK).body(searchBook);
    }

    /**
     * 도서 재고 상태 업데이트 @PatchMapping("/{id}/availability")
     */
    @PatchMapping("/{id}/availability")
    public ResponseEntity<Book> updateBookAvailability(@PathVariable Long id, Boolean available) {
        log.info("도서 재고 상태 수정 요청 - ID: {}", id);
        Book updated = bookService.updateBookAvailability(id, available);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    /**
     * ISBN 중복 확인 @GetMapping("/validate/isbn")
     */
    @GetMapping("/validate/isbn")
    public ResponseEntity<Boolean> isIsbnExists(String isbn) {
        log.info("ISBN 중복 확인 요청 - ISBN: {}",  isbn);
        boolean isbnExists = bookService.isIsbnExists(isbn);
        return ResponseEntity.status(HttpStatus.OK).body(isbnExists);
    }

    /**
     * 도서 통계 조회 @GetMapping("/statistics/total")
     */
    @GetMapping("/statistics/total")
    public long getTotalBooksCount() {
        log.info("도서 통계 조회 요청");
        long totalBooksCount = bookService.getTotalBooksCount();
        return totalBooksCount;
    }

    /**
     * 도서 통계 조회 @GetMapping("/statistics/active")
     */
    @GetMapping("/statistics/active")
    public long getActiveBooksCount() {
        log.info("도서 통계 조회 요청");
        long activeBooksCount = bookService.getActiveBooksCount();
        return activeBooksCount;
    }
}
