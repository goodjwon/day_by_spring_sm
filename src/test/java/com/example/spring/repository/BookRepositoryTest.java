package com.example.spring.repository;


import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import com.example.spring.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(com.example.spring.repository.impl.JpaBookRepository.class)
public class BookRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn(ISBN.of("9780132350884"))
                .price(Money.of(new BigDecimal("45.99")))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();
    }

    @Test
    public void save_신규도서_저장성공() {
        //Given
        Book newBook = Book.builder()
                .author("새 저자")
                .title("새 도서")
                .price(Money.of(new BigDecimal("10000")))
                .isbn(ISBN.of("9780132350885")) // 유니크한 ISBN 생성
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();


        //When
        Book savedBook = bookRepository.save(newBook);
        entityManager.flush(); // 즉시 저장

        //Then
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo(newBook.getTitle());
        assertThat(savedBook.getAuthor()).isEqualTo(newBook.getAuthor());
        assertThat(savedBook.getPrice()).isEqualTo(newBook.getPrice());

        bookRepository.deleteById(savedBook.getId());


    }

    @Test
    public void findById_존재하는도서_도서반환() {
        //Given
        entityManager.persistAndFlush(sampleBook);
        entityManager.clear();

        //When
        Optional<Book> foundBook = bookRepository.findById(sampleBook.getId());

        //Then
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("Clean Code");
        assertThat(foundBook.get().getAuthor()).isEqualTo("Robert C. Martin");

    }

    @Test
    public void findById_존재하지않는도서_빈Optional반환() {
        Long bookId = 150L;

        Optional<Book> foundBook = bookRepository.findById(bookId);

        assertThat(foundBook).isEmpty();

    }

    @Test
    public void findAll_도서목록반환() {
        //Given
        Book book1 = Book.builder()
                .title("Effective Java")
                .author("Joshua Bloch")
                .isbn(ISBN.of("9780134685991"))
                .price(Money.of(new BigDecimal("52.99")))
                .createdDate(LocalDateTime.now().minusDays(1))
                .build();

        Book book2 = Book.builder()
                .title("Spring in Action")
                .author("Craig Walls")
                .isbn(ISBN.of("9781617294945"))
                .price(Money.of(new BigDecimal("39.99")))
                .createdDate(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(book1);
        entityManager.persistAndFlush(book2);
        entityManager.clear();

        // When
        List<Book> books = bookRepository.findAll();

        // Then
        assertThat(books).isNotEmpty();
        assertThat(books).hasSize(2);
    }

    @Test
    public void save_기존도서_업데이트성공() {
        // Given - 데이터베이스에 이미 존재하는 도서 사용 (ID: 2)
        Book existingBook = bookRepository.findById(2L).get();
        String originalTitle = existingBook.getTitle();
        String originalAuthor = existingBook.getAuthor();

        // When
        existingBook.setTitle("수정된 도서");
        existingBook.setAuthor("수정된 저자");
        Book updatedBook = bookRepository.save(existingBook);
        entityManager.flush();
        entityManager.clear();

        // Then
        Book reloadedBook = bookRepository.findById(2L).get();
        assertThat(reloadedBook.getTitle()).isEqualTo("수정된 도서");
        assertThat(reloadedBook.getAuthor()).isEqualTo("수정된 저자");

        // Restore original values for other tests
        reloadedBook.setTitle(originalTitle);
        reloadedBook.setAuthor(originalAuthor);
        bookRepository.save(reloadedBook);

    }

    @Test
    public void deleteById_도서삭제() {

        entityManager.persistAndFlush(sampleBook);

        // When
        bookRepository.deleteById(sampleBook.getId());
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Book> deletedBook = bookRepository.findById(sampleBook.getId());
        assertThat(deletedBook).isEmpty();
    }

    @Test
    public void findBookById_존재하는도서_도서직접반환() {
        //Given
        entityManager.persistAndFlush(sampleBook);
        //When
        Book foundBook = bookRepository.findByIdOrThrow(sampleBook.getId());
        //Then
        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getId()).isEqualTo(sampleBook.getId());
        assertThat(foundBook.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    public void findBookById_존재하지않는도서_null반환() {
        // Given
        Long bookId = 999L;

        // When
        Book foundBook = bookRepository.findByIdOrThrow(bookId);

        // Then
        assertThat(foundBook).isNull();
    }

    @Nested
    @DisplayName("검색 기능 테스트")
    class SearchTest {

        @BeforeEach
        void setUpBooks() {
            Book cleanCode = Book.builder()
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn(ISBN.of("9780132350884"))
                    .price(Money.of(new BigDecimal("45.99")))
                    .createdDate(LocalDateTime.now())
                    .build();

            Book effectiveJava = Book.builder()
                    .title("Effective Java")
                    .author("Joshua Bloch")
                    .isbn(ISBN.of("9780134685991"))
                    .price(Money.of(new BigDecimal("52.99")))
                    .createdDate(LocalDateTime.now())
                    .build();

            Book springInAction = Book.builder()
                    .title("Spring in Action")
                    .author("Craig Walls")
                    .isbn(ISBN.of("9781617294945"))
                    .price(Money.of(new BigDecimal("39.99")))
                    .createdDate(LocalDateTime.now())
                    .build();

            entityManager.persistAndFlush(cleanCode);
            entityManager.persistAndFlush(effectiveJava);
            entityManager.persistAndFlush(springInAction);
            entityManager.clear();
        }

        // Test 구현.
    }

    @Nested
    @DisplayName("재고 상태별 테스트")
    class AvailabilityTest {

        @BeforeEach
        void setUpBooksWithAvailability() {
            Book availableBook = Book.builder()
                    .title("Available Book")
                    .author("Author A")
                    .isbn(ISBN.of("1111111111111"))
                    .price(Money.of(new BigDecimal("30.00")))
                    .available(true)
                    .createdDate(LocalDateTime.now())
                    .build();

            Book unavailableBook = Book.builder()
                    .title("Unavailable Book")
                    .author("Author B")
                    .isbn(ISBN.of("2222222222222"))
                    .price(Money.of(new BigDecimal("25.00")))
                    .available(false)
                    .createdDate(LocalDateTime.now())
                    .build();

            entityManager.persistAndFlush(availableBook);
            entityManager.persistAndFlush(unavailableBook);
            entityManager.clear();
        }

        //Test 구현.
    }
}
