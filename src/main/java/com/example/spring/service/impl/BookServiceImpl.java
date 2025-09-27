package com.example.spring.service.impl;

import com.example.spring.entity.Book;
import com.example.spring.exception.BookException;
import com.example.spring.exception.BusinessException;
import com.example.spring.exception.EntityNotFoundException;
import com.example.spring.repository.BookRepository;
import com.example.spring.service.BookService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
public class BookServiceImpl implements BookService {

    private BookRepository bookRepository;
    private ApplicationEventPublisher eventPublisher;

    //todo sub-task 작성 필요 to github.
    @Override
    public Book createBook(Book book) {
        log.info("도서 생성 요청 - 도서명: {}", book.getTitle());

        if(bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BookException.DuplicateIsbnException("이미 존재하는 ISBN입니다: " + book.getIsbn());
        }

        Book savedBook = bookRepository.save(book);

        log.info("도서 생성 완료 - ID : {}",  savedBook.getId());

        return savedBook;
    }

    @Override
    public Optional<Book> getBookById(Long id) {
        log.info("ID로 도서 검색 - ID: {}", id);
        return bookRepository.findById(id);
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
    public Book updateBook(Long id, Book book) {
        //도서가 있어야함.
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("도서를 찾을 수 없어요" + id));

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
        existingBook.setUpdatedDate(book.getUpdatedDate());

        Book savedBook = bookRepository.save(existingBook);

        log.info("도서 정보 수정 완료 - ID: {}", savedBook.getId());

        return savedBook;
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
        return bookRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    public Page<Book> searchBooksWithFilters(String title, String author, BigDecimal minPrice, BigDecimal maxPrice, Boolean available, Pageable pageable) {
        return null;
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
