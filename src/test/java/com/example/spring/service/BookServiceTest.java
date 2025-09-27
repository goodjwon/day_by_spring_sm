package com.example.spring.service;

import com.example.spring.repository.BookRepository;
import com.example.spring.service.impl.BookServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@DataJpaTest
public class BookServiceTest {
    private BookServiceImpl bookService;
    private BookRepository bookRepository;
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("도서 생성 - 성공")
    void createBook_성공() {
        //Given
        //When
        //Then
    }
}
