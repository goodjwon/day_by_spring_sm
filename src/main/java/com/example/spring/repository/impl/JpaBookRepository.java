package com.example.spring.repository.impl;

import com.example.spring.entity.Book;
import com.example.spring.repository.BookRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA를 사용한 Repository 구현
 * - 간단한 인터페이스 선언만으로 CRUD 기능 자동 생성
 * - 복잡한 JDBC 코드 제거
 */
@Repository
public class JpaBookRepository implements BookRepository {
    // JpaRepository가 기본 CRUD 메서드들을 자동으로 제공
    // findById, save, findAll, deleteById 등이 자동으로 구현됨

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Book> findById(Long id) {
        Book book = em.find(Book.class, id);
        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        return em.createQuery("SELECT b FROM Book b ORDER BY b.createdDate DESC", Book.class)
                .getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == null) {
            em.persist(book);
            return book;
        } else  {
            return em.merge(book);
        }
    }

    @Override
    public void deleteById(Long id) {
        Book book = em.find(Book.class, id);
        if (book != null) {
            em.remove(book);
        }
    }

    @Override
    public Optional<Book> findByISBN(String isbn) {
        TypedQuery<Book> query = em.createQuery("SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class);
        query.setParameter("isbn", isbn);

        List<Book> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        Long count = em.createQuery("SELECT COUNT(b) FROM Book b WHERE b.isbn = :isbn", Long.class)
                .setParameter("isbn", isbn)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public List<Book> findByTitleContaining(String title) {
        return em.createQuery("SELECT b FROM Book b WHERE b.title LIKE :title ORDER BY b.title")
                .setParameter("title", "%" + title + "%")
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findByAuthorContaining(String author) {
        return em.createQuery("SELECT b FROM Book b WHERE b.author LIKE :author ORDER BY b.author",  Book.class)
                .setParameter("author", "%" + author + "%")
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findByTitleContainingOrAuthorContaining(String title, String author) {
        return em.createQuery("SELECT b FROM Book b WHERE b.title LIKE :title OR b.author LIKE :author ORDER BY b.title", Book.class)
                .setParameter("title", "%" + title + "%")
                .setParameter("author", "%" + author + "%")
                .getResultList();
    }

    @Override
    public List<Book> findByAvailableTrue(boolean available) {
        return em.createQuery(
                        "SELECT b FROM Book b WHERE b.available = :available ORDER BY b.createdDate DESC",
                        Book.class)
                .setParameter("available", available)
                .getResultList();
    }

    @Override
    public List<Book> findByDeletedDateIsNull() {
        return em.createQuery(
                        "SELECT b FROM Book b WHERE b.deletedDate IS NULL ORDER BY b.createdDate DESC",
                        Book.class)
                .getResultList();
    }

    @Override
    public List<Book> findByDeletedDateIsNotNull() {
        return em.createQuery(
                        "SELECT b FROM Book b WHERE b.deletedDate IS NOT NULL ORDER BY b.deletedDate DESC",
                        Book.class)
                .getResultList();
    }

    @Override
    public List<Book> findByPriceBetween(BigDecimal lowPrice, BigDecimal highPrice) {
        return em.createQuery(
                        "SELECT b FROM Book b WHERE b.price BETWEEN :minPrice AND :maxPrice ORDER BY b.price",
                        Book.class)
                .setParameter("minPrice", lowPrice)
                .setParameter("maxPrice", highPrice)
                .getResultList();
    }
}

