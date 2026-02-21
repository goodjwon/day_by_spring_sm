package com.example.spring.application.service;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import com.example.spring.application.dto.request.CreateBookRequest;
import com.example.spring.application.dto.request.UpdateBookRequest;
import com.example.spring.application.dto.response.BookResponse;
import com.example.spring.domain.model.Book;
import com.example.spring.exception.BookException;
import com.example.spring.domain.repository.BookRepository;
import com.example.spring.domain.repository.BookSpecification;
import com.example.spring.application.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

/**
 * BookService 구현체
 * 도서 관리 비즈니스 로직을 구현합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) {

        // Value Object 생성 전 유효성 검사
        validateCreateRequest(request);

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookException.DuplicateIsbnException("이미 존재하는 ISBN입니다: " + request.getIsbn());
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(ISBN.of(request.getIsbn()))
                .price(Money.of(request.getPrice()))
                .available(request.getAvailable())
                .coverImageUrl(request.getCoverImageUrl())
                .createdDate(LocalDateTime.now())
                .build();

        validateBook(book);
        Book savedBook = bookRepository.save(book);
        return BookResponse.from(savedBook);
    }

    private void validateCreateRequest(CreateBookRequest request) {
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
    }

    @Override
    @Cacheable(value = "books", key = "#id")
    public Optional<BookResponse> getBookById(Long id) {
        return bookRepository.findById(id)
                .filter(book -> book.getDeletedDate() == null)
                .map(BookResponse::from);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .filter(book -> book.getDeletedDate() == null);
    }

    @Override
    public Page<Book> getAllActiveBooks(Pageable pageable) {

        List<Book> allActiveBooks = bookRepository.findByDeletedDateIsNull();

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allActiveBooks.size());

        List<Book> pageContent = start >= allActiveBooks.size() ? List.of() : allActiveBooks.subList(start, end);

        return new PageImpl<>(pageContent, pageable, allActiveBooks.size());
    }

    @Override
    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public BookResponse updateBook(Long id, UpdateBookRequest request) {

        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookException.BookNotFoundException(id));

        if (existingBook.getDeletedDate() != null) {
            throw new BookException.DeletedBookAccessException("삭제된 도서는 수정할 수 없습니다: " + id);
        }

        // ISBN 중복 검사 (자기 자신 제외)
        String existingIsbnValue = existingBook.getIsbn() != null ? existingBook.getIsbn().getValue() : null;
        if (!request.getIsbn().equals(existingIsbnValue) &&
                bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookException.DuplicateIsbnException("이미 존재하는 ISBN입니다: " + request.getIsbn());
        }

        // 필드 업데이트
        existingBook.updateBookInfo(request.getTitle(), request.getAuthor(), ISBN.of(request.getIsbn()));
        existingBook.updatePrice(Money.of(request.getPrice()));
        existingBook.setAvailability(request.getAvailable());
        existingBook.updateCoverImageUrl(request.getCoverImageUrl());

        validateBook(existingBook);
        Book savedBook = bookRepository.save(existingBook);
        return BookResponse.from(savedBook);
    }

    @Override
    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public void deleteBook(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookException.BookNotFoundException(id));

        if (book.getDeletedDate() != null) {
            throw new BookException.DeletedBookAccessException("이미 삭제된 도서입니다: " + id);
        }

        book.markAsDeleted();
        bookRepository.save(book);
    }

    @Override
    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public void restoreBook(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookException.BookNotFoundException(id));

        if (book.getDeletedDate() == null) {
            throw new BookException.InvalidBookStateException("삭제되지 않은 도서는 복원할 수 없습니다: " + id);
        }

        book.restore();
        bookRepository.save(book);
    }

    @Override
    public List<Book> searchByTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return List.of();
        }
        return bookRepository.findByTitleContainingIgnoreCase(title).stream()
                .filter(book -> book.getDeletedDate() == null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        if (!StringUtils.hasText(author)) {
            return List.of();
        }
        return bookRepository.findByAuthorContainingIgnoreCase(author).stream()
                .filter(book -> book.getDeletedDate() == null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> searchByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        return bookRepository.findByTitleContainingOrAuthorContaining(keyword).stream()
                .filter(book -> book.getDeletedDate() == null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {

        if (minPrice == null && maxPrice == null) {
            return bookRepository.findByDeletedDateIsNull();
        }

        if (minPrice == null) {
            minPrice = BigDecimal.ZERO;
        }
        if (maxPrice == null) {
            maxPrice = new BigDecimal("999999.99");
        }

        if (minPrice.compareTo(maxPrice) > 0) {
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

        List<Book> allActiveBooks = bookRepository.findByDeletedDateIsNull();

        // 필터링 적용
        List<Book> filteredBooks = allActiveBooks.stream()
                .filter(book -> title == null || book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(book -> author == null || book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .filter(book -> minPrice == null || book.getPrice().getAmount().compareTo(minPrice) >= 0)
                .filter(book -> maxPrice == null || book.getPrice().getAmount().compareTo(maxPrice) <= 0)
                .filter(book -> available == null || book.getAvailable().equals(available))
                .collect(Collectors.toList());

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredBooks.size());

        List<Book> pageContent = start >= filteredBooks.size() ? List.of() : filteredBooks.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filteredBooks.size());
    }

    @Override
    public Page<Book> searchBooksWithQueryFilters(String title, String author,
                                                  BigDecimal minPrice, BigDecimal maxPrice,
                                                  Boolean available, Pageable pageable) {

        return bookRepository.findAll(
                BookSpecification.withFilters(title, author, minPrice, maxPrice, available),
                pageable);
    }

    @Override
    public List<Book> getBooksByAvailability(Boolean available) {
        return bookRepository.findByAvailable(available).stream()
                .filter(book -> book.getDeletedDate() == null)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isIsbnExists(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    @Override
    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public Book updateBookAvailability(Long id, Boolean available) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookException.BookNotFoundException(id));

        if (book.getDeletedDate() != null) {
            throw new BookException.DeletedBookAccessException("삭제된 도서의 재고를 수정할 수 없습니다: " + id);
        }

        book.setAvailability(available);

        Book savedBook = bookRepository.save(book);
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

    /**
     * 도서 유효성 검증
     */
    private void validateBook(Book book) {
        if (book == null) {
            throw new BookException.InvalidBookDataException("도서 정보가 null입니다");
        }
        if (!StringUtils.hasText(book.getTitle())) {
            throw new BookException.InvalidBookDataException("도서 제목은 필수입니다");
        }
        if (!StringUtils.hasText(book.getAuthor())) {
            throw new BookException.InvalidBookDataException("저자는 필수입니다");
        }
        if (book.getIsbn() == null || !StringUtils.hasText(book.getIsbn().getValue())) {
            throw new BookException.InvalidBookDataException("ISBN은 필수입니다");
        }
        if (book.getPrice() == null || book.getPrice().getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BookException.InvalidBookDataException("가격은 0 이상이어야 합니다");
        }
    }
}