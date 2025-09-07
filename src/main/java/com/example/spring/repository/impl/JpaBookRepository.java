package com.example.spring.repository.impl;

import com.example.spring.entity.Book;
import com.example.spring.repository.BookRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA를 사용한 Repository 구현
 * - 간단한 인터페이스 선언만으로 CRUD 기능 자동 생성
 * - 복잡한 JDBC 코드 제거
 */
@Repository
public interface JpaBookRepository extends BookRepository {
    // JpaRepository가 기본 CRUD 메서드들을 자동으로 제공
    // findById, save, findAll, deleteById 등이 자동으로 구현됨
}

