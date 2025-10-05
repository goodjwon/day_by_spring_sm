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

    private BookRepository bookRepository;
    private ApplicationEventPublisher eventPublisher;

    //todo sub-task 작성 필요 to github.
    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        log.info("도서 생성 요청 - 도서명: {}", request.getTitle());

        if(bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookException.DuplicateIsbnException("이미 존재하는 ISBN입니다: " + request.getIsbn());
        }

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookException.DuplicateIsbnException(request.getIsbn());
        }
        Book book = Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .price(request.getPrice())
                .build();

        Book savedBook = bookRepository.save(book);

        eventPublisher.publishEvent(bookRepository.save(savedBook));

        log.info("도서 생성 완료 - ID : {}",  savedBook.getId());

        return BookResponse.from(savedBook);
    }

    @Override
    public BookResponse getBookById(Long id) {
        log.info("ID로 도서 검색 - ID: {}", id);
        return BookResponse.from(bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book", id)));
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findByISBN(isbn);
    }

    //todo 나중에 할것
    @Override
    public Page<Book> getAllActiveBooks(Pageable pageable) {
        return null;
    }

    @Override
    public BookResponse updateBook(Long id, UpdateBookRequest book) {
        //도서가 있어야함.
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("도서를 찾을 수 없습니다" + id));

        //소프트 딜리트
        if (existingBook.getDeletedDate() != null) {
            throw new BookException.DeletedBookAccessException("삭제된 도서는 수정할 수 없습니다: " + id);
        }

        // ISBN 중복 검사 (자기 자신 제외)
        if (!existingBook.getIsbn().equals(book.getIsbn()) &&
                bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BookException.DuplicateIsbnException("이미 존재하는 ISBN입니다: " + book.getIsbn());
        }

        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setIsbn(book.getIsbn());
        existingBook.setPrice(book.getPrice());
        existingBook.setAvailable(book.getAvailable());

        Book savedBook = bookRepository.save(existingBook);

        log.info("도서 정보 수정 완료 - ID: {}", savedBook.getId());

        return BookResponse.from(savedBook);
    }

    @Override
    public void deleteBook(Long id) {
        log.info("도서 삭제 요청 (Soft Delete) - ID: {}", id);
        Book book = bookRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("삭제할 도서를 찾지 못하였습니다. ID: " + id));

        if (book.getDeletedDate() != null) {
            log.warn("이미 삭제된 도서입니다. ID: {}", id);
            return;
        }

        book.setDeletedDate(LocalDateTime.now());
        bookRepository.save(book);
        log.info("도서 삭제 완료 (Soft Delete) - ID: {}", id);
    }

    @Override
    public void restoreBook(Long id) {
        log.info("도서 복원 요청 - ID: {}", id);
        Book book = bookRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("복원할 도서를 찾지 못하였습니다. ID: " + id));

        if (book.getDeletedDate() == null) {
            log.warn("이미 활성 상태인 도서입니다. ID: {}", id);
            return;
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

        if(minPrice == null && maxPrice == null) {
            return bookRepository.findByDeletedDateIsNotNull();
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

        return bookRepository.findByPriceBetween(minPrice, maxPrice).stream().filter(book -> book.getDeletedDate() == null ).collect(Collectors.toList());
    }

    @Override
    public Page<Book> searchBooksWithFilters(String title, String author,
                                             BigDecimal minPrice, BigDecimal maxPrice,
                                             Boolean available, Pageable pageable) {

        log.debug("복합 조건으로 도서 검색 - 제목: {}, 저자: {}, 최소가격: {}, 최대가격: {}, 재고: {}, page: {}, size: {}",
                title, author, minPrice, maxPrice, available, pageable.getPageNumber(), pageable.getPageSize());


        List<Book> allActiveBooks = bookRepository.findByDeletedDateIsNotNull();

        // 가정. 책이 100만권 있어요. 알라딘? 예스24?

        //1. java 에서 필터링 하는 방법
         // - 필터를 하는게 나음.      10001~ 200000
        List<Book> filteredBooks = allActiveBooks.stream()
                .filter(book -> title == null || book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(book -> author == null || book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .filter(book -> minPrice == null || book.getPrice().compareTo(minPrice) >= 0)
                .filter(book -> maxPrice == null || book.getPrice().compareTo(maxPrice) <= 0)
                .filter(book -> available == null || book.getAvailable().equals(available))
                .collect(Collectors.toList());


        // 100만개를 로딩한 후에 처리. => was (tomcat) 죽어요. outOfMemory. Die...
        // 처리 효율이 떨어짐.
        // 처리가 쉬워요.

        //todo 2. db에서 필터링 하는 방법으로 개선하기.
        // 효율이 좋고.
        // 코드가 어려워요.

        // 페이징 처리
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
