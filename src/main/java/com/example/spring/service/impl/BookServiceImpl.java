package com.example.spring.service.impl;

import com.example.spring.dto.request.CreateBookRequest;
import com.example.spring.dto.request.UpdateBookRequest;
import com.example.spring.dto.response.BookResponse;
import com.example.spring.entity.Book;
import com.example.spring.exception.BookException;
import com.example.spring.exception.EntityNotFoundException;
import com.example.spring.repository.BookRepository;
import com.example.spring.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        log.info("도서 생성 요청 - 도서명: {}", request.getTitle());

        // 유효성 검사
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BookException.InvalidBookDataException("도서 제목은 필수입니다");
        }
        if (!StringUtils.hasText(request.getAuthor())) {
            throw new BookException.InvalidBookDataException("저자는 필수입니다");
        }
        if (!StringUtils.hasText(request.getIsbn())) {
            throw new BookException.InvalidBookDataException("ISBN은 필수입니다");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BookException.InvalidBookDataException("가격은 0 이상이어야 합니다");
        }
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookException.DuplicateIsbnException("이미 존재하는 ISBN입니다");
        }

        Book book = Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .price(request.getPrice())
                .available(request.getAvailable())
                .build();

        Book savedBook = bookRepository.save(book);

        // eventPublisher.publishEvent(savedBook); // 이벤트 발행 로직 (필요시 활성화)

        log.info("도서 생성 완료 - ID : {}", savedBook.getId());

        return BookResponse.from(savedBook);
    }

    @Override
    public BookResponse getBookById(Long id) {
        log.info("ID로 도서 검색 - ID: {}", id);
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            return null;
        }
        return BookResponse.from(book);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findByISBN(isbn);
    }

    @Override
    public Page<Book> getAllActiveBooks(Pageable pageable) {
        List<Book> activeBooks = bookRepository.findByDeletedDateIsNull();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), activeBooks.size());
        List<Book> pageContent = start >= activeBooks.size() ? List.of() : activeBooks.subList(start, end);
        return new PageImpl<>(pageContent, pageable, activeBooks.size());
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, UpdateBookRequest request) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("도서를 찾을 수 없습니다: " + id));

        if (existingBook.getDeletedDate() != null) {
            throw new BookException.DeletedBookAccessException("삭제된 도서는 수정할 수 없습니다");
        }

        if (!existingBook.getIsbn().equals(request.getIsbn()) &&
                bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookException.DuplicateIsbnException("이미 존재하는 ISBN입니다: " + request.getIsbn());
        }

        existingBook.setTitle(request.getTitle());
        existingBook.setAuthor(request.getAuthor());
        existingBook.setIsbn(request.getIsbn());
        existingBook.setPrice(request.getPrice());
        existingBook.setAvailable(request.getAvailable());
        existingBook.setUpdatedDate(LocalDateTime.now());

        Book savedBook = bookRepository.save(existingBook);
        log.info("도서 정보 수정 완료 - ID: {}", savedBook.getId());
        return BookResponse.from(savedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        log.info("도서 삭제 요청 (Soft Delete) - ID: {}", id);
        Book book = bookRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("삭제할 도서를 찾지 못하였습니다."));

        if (book.getDeletedDate() != null) {
            throw new BookException.DeletedBookAccessException("이미 삭제된 도서입니다.");
        }

        book.setDeletedDate(LocalDateTime.now());
        bookRepository.save(book);
        log.info("도서 삭제 완료 (Soft Delete) - ID: {}", id);
    }

    @Override
    @Transactional
    public void restoreBook(Long id) {
        log.info("도서 복원 요청 - ID: {}", id);
        Book book = bookRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("복원할 도서를 찾지 못하였습니다."));

        if (book.getDeletedDate() == null) {
            throw new BookException.InvalidBookStateException("삭제되지 않은 도서는 복원할 수 없습니다");
        }

        book.setDeletedDate(null);
        bookRepository.save(book);
        log.info("도서 복원 완료 - ID: {}", id);
    }

    @Override
    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContaining(title);
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        return bookRepository.findByAuthorContaining(author);
    }



    @Override
    public List<Book> searchByKeyword(String keyword) {
        return bookRepository.findByTitleContainingOrAuthorContaining(keyword, keyword);
    }

    @Override
    public List<Book> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("가격 범위로 도서 검색 - 최소: {}, 최대: {}", minPrice, maxPrice);

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BookException.InvalidPriceRangeException("최소 가격이 최대 가격보다 클 수 없습니다");
        }

        return bookRepository.findByPriceBetween(minPrice, maxPrice).stream()
                .filter(book -> book.getDeletedDate() == null)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Book> searchBooksWithFilters(String title, String author,
                                             BigDecimal minPrice, BigDecimal maxPrice,
                                             Boolean available, Pageable pageable) {

        log.debug("복합 조건으로 도서 검색 - 제목: {}, 저자: {}, 최소가격: {}, 최대가격: {}, 재고: {}, page: {}, size: {}",
                title, author, minPrice, maxPrice, available, pageable.getPageNumber(), pageable.getPageSize());

        List<Book> allActiveBooks = bookRepository.findByDeletedDateIsNull();

        List<Book> filteredBooks = allActiveBooks.stream()
                .filter(book -> !StringUtils.hasText(title) || book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(book -> !StringUtils.hasText(author) || book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .filter(book -> minPrice == null || book.getPrice().compareTo(minPrice) >= 0)
                .filter(book -> maxPrice == null || book.getPrice().compareTo(maxPrice) <= 0)
                .filter(book -> available == null || book.getAvailable().equals(available))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredBooks.size());

        List<Book> pageContent = start >= filteredBooks.size() ? List.of() : filteredBooks.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filteredBooks.size());
    }

    @Override
    public List<Book> getBooksByAvailability(Boolean available) {
        return bookRepository.findByAvailableTrue(available);
    }

    @Override
    public boolean isIsbnExists(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    @Override
    @Transactional
    public Book updateBookAvailability(Long id, Boolean available) {
        log.info("도서 재고 상태 변경 요청 - ID: {}, 상태: {}", id, available);
        Book book = bookRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("상태를 변경할 도서를 찾을 수 없습니다. ID: " + id));

        book.setAvailable(available);
        book.setUpdatedDate(LocalDateTime.now());

        Book savedBook = bookRepository.save(book);
        log.info("도서 재고 상태 변경 완료 - ID: {}", id);
        return savedBook;
    }

    @Override
    public long getTotalBooksCount() {
        return bookRepository.findAll().size();
    }

    @Override
    public long getActiveBooksCount() {
        return bookRepository.findByDeletedDateIsNull().size();
    }
}
