package com.example.spring.repository.impl;

import com.example.spring.entity.Book;
import com.example.spring.repository.BookRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA를 사용한 Repository 구현
 * - 간단한 인터페이스 선언만으로 CRUD 기능 자동 생성
 * - 복잡한 JDBC 코드 제거
 */
@Repository
@Transactional
public interface JpaBookRepository extends BookRepository {
    // JpaRepository가 기본 CRUD 메서드들을 자동으로 제공
    // findById, save, findAll, deleteById 등이 자동으로 구현됨
/*
    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Book> findById(Long id) {
        Book book = em.find(Book.class, id);
        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        return List.of();
    }

    @Override
    public Book save(Book book) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public Book findByIdOrThrow(Long id) {
        return BookRepository.super.findByIdOrThrow(id);
    }*/
}

