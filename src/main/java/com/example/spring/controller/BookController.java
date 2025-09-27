package com.example.spring.controller;

import com.example.spring.dto.request.CreateBookRequest;
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

import java.util.List;

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
    @GetMapping
    public ResponseEntity<BookResponse> findBookById(@PathVariable Long id) {
        log.info("도서 조회 요청 - ID: {}", id);
        BookResponse response = bookService.getBookById(id);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
//    도서 정보 수정 API (PUT /api/books/{id})
//    도서 삭제 API (DELETE /api/books/{id}) - Soft Delete
//    ISBN 중복 검증
}
