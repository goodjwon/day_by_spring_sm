package com.example.spring.domain.repository;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("BookRepository 통합 테스트")
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

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class BasicCrudTest {

        @Test
        @DisplayName("도서 저장 성공")
        void save_신규도서_저장성공() {
            // When
            Book savedBook = bookRepository.save(sampleBook);
            entityManager.flush();

            // Then
            assertThat(savedBook.getId()).isNotNull();
            assertThat(savedBook.getTitle()).isEqualTo("Clean Code");
            assertThat(savedBook.getAuthor()).isEqualTo("Robert C. Martin");
            assertThat(savedBook.getIsbn().getDigitsOnly()).isEqualTo("9780132350884");
            assertThat(savedBook.getPrice().getAmount()).isEqualByComparingTo(new BigDecimal("45.99"));
            assertThat(savedBook.getAvailable()).isTrue();
        }

        @Test
        @DisplayName("ID로 도서 조회 성공")
        void findById_존재하는도서_도서반환() {
            // Given
            Book persistedBook = entityManager.persistAndFlush(sampleBook);
            entityManager.clear();

            // When
            Optional<Book> foundBook = bookRepository.findById(persistedBook.getId());

            // Then
            assertThat(foundBook).isPresent();
            assertThat(foundBook.get().getTitle()).isEqualTo("Clean Code");
            assertThat(foundBook.get().getIsbn().getDigitsOnly()).isEqualTo("9780132350884");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
        void findById_존재하지않는ID_빈Optional반환() {
            // When
            Optional<Book> foundBook = bookRepository.findById(999L);

            // Then
            assertThat(foundBook).isEmpty();
        }

        @Test
        @DisplayName("모든 도서 조회")
        void findAll_모든도서조회() {
            // Given
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
            assertThat(books).hasSize(2);
            // 모든 도서가 포함되어 있는지 확인
            assertThat(books).extracting(Book::getTitle)
                    .containsExactlyInAnyOrder("Effective Java", "Spring in Action");
        }

        @Test
        @DisplayName("도서 삭제 성공")
        void deleteById_존재하는도서_삭제성공() {
            // Given
            Book persistedBook = entityManager.persistAndFlush(sampleBook);
            Long bookId = persistedBook.getId();
            entityManager.clear();

            // When
            bookRepository.deleteById(bookId);
            entityManager.flush();

            // Then
            Optional<Book> deletedBook = bookRepository.findById(bookId);
            assertThat(deletedBook).isEmpty();
        }
    }

    @Nested
    @DisplayName("ISBN 관련 테스트")
    class IsbnTest {

        @Test
        @DisplayName("ISBN으로 도서 조회 성공")
        void findByIsbn_존재하는ISBN_도서반환() {
            // Given
            entityManager.persistAndFlush(sampleBook);
            entityManager.clear();

            // When
            Optional<Book> foundBook = bookRepository.findByIsbn("9780132350884");

            // Then
            assertThat(foundBook).isPresent();
            assertThat(foundBook.get().getTitle()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("존재하지 않는 ISBN으로 조회 시 빈 Optional 반환")
        void findByIsbn_존재하지않는ISBN_빈Optional반환() {
            // When
            Optional<Book> foundBook = bookRepository.findByIsbn("9999999999999");

            // Then
            assertThat(foundBook).isEmpty();
        }

        @Test
        @DisplayName("ISBN 존재 여부 확인 - 존재하는 경우")
        void existsByIsbn_존재하는ISBN_true반환() {
            // Given
            entityManager.persistAndFlush(sampleBook);

            // When
            boolean exists = bookRepository.existsByIsbn("9780132350884");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("ISBN 존재 여부 확인 - 존재하지 않는 경우")
        void existsByIsbn_존재하지않는ISBN_false반환() {
            // When
            boolean exists = bookRepository.existsByIsbn("9999999999999");

            // Then
            assertThat(exists).isFalse();
        }
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

        @Test
        @DisplayName("제목으로 도서 검색")
        void findByTitleContaining_부분제목_해당도서들반환() {
            // When
            List<Book> books = bookRepository.findByTitleContainingIgnoreCase("Java");

            // Then
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getTitle()).isEqualTo("Effective Java");
        }

        @Test
        @DisplayName("저자로 도서 검색")
        void findByAuthorContaining_부분저자명_해당도서들반환() {
            // When
            List<Book> books = bookRepository.findByAuthorContainingIgnoreCase("Martin");

            // Then
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getAuthor()).isEqualTo("Robert C. Martin");
        }

        @Test
        @DisplayName("제목 또는 저자로 도서 검색")
        void findByTitleContainingOrAuthorContaining_키워드_해당도서들반환() {
            // When
            List<Book> books = bookRepository.findByTitleContainingOrAuthorContaining("Spring");

            // Then
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getTitle()).isEqualTo("Spring in Action");
        }

        @Test
        @DisplayName("가격 범위로 도서 검색")
        void findByPriceBetween_가격범위_해당도서들반환() {
            // When
            List<Book> books = bookRepository.findByPriceBetween(
                    new BigDecimal("40.00"), new BigDecimal("50.00"));

            // Then
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getTitle()).isEqualTo("Clean Code");
            assertThat(books.get(0).getPrice().getAmount()).isEqualByComparingTo(new BigDecimal("45.99"));
        }
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

        @Test
        @DisplayName("재고 상태별 도서 조회")
        void findByAvailable_재고상태_해당도서들반환() {
            // When
            List<Book> availableBooks = bookRepository.findByAvailable(true);
            List<Book> unavailableBooks = bookRepository.findByAvailable(false);

            // Then
            assertThat(availableBooks).hasSize(1);
            assertThat(availableBooks.get(0).getTitle()).isEqualTo("Available Book");

            assertThat(unavailableBooks).hasSize(1);
            assertThat(unavailableBooks.get(0).getTitle()).isEqualTo("Unavailable Book");
        }

        @Test
        @DisplayName("재고 상태와 제목으로 복합 검색")
        void findByAvailableAndTitleContaining_복합조건_해당도서들반환() {
            // When
            List<Book> books = bookRepository.findByAvailableAndTitleContaining(true, "Available");

            // Then
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getTitle()).isEqualTo("Available Book");
            assertThat(books.get(0).getAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("Soft Delete 테스트")
    class SoftDeleteTest {

        @Test
        @DisplayName("삭제되지 않은 도서만 조회")
        void findByDeletedDateIsNull_활성도서만_반환() {
            // Given
            Book activeBook = Book.builder()
                    .title("Active Book")
                    .author("Author A")
                    .isbn(ISBN.of("1111111111111"))
                    .price(Money.of(new BigDecimal("30.00")))
                    .createdDate(LocalDateTime.now())
                    .build();

            Book deletedBook = Book.builder()
                    .title("Deleted Book")
                    .author("Author B")
                    .isbn(ISBN.of("2222222222222"))
                    .price(Money.of(new BigDecimal("25.00")))
                    .createdDate(LocalDateTime.now())
                    .deletedDate(LocalDateTime.now())
                    .build();

            entityManager.persistAndFlush(activeBook);
            entityManager.persistAndFlush(deletedBook);
            entityManager.clear();

            // When
            List<Book> activeBooks = bookRepository.findByDeletedDateIsNull();

            // Then
            assertThat(activeBooks).hasSize(1);
            assertThat(activeBooks.get(0).getTitle()).isEqualTo("Active Book");
        }

        @Test
        @DisplayName("삭제된 도서만 조회")
        void findByDeletedDateIsNotNull_삭제된도서만_반환() {
            // Given
            Book activeBook = Book.builder()
                    .title("Active Book")
                    .author("Author A")
                    .isbn(ISBN.of("1111111111111"))
                    .price(Money.of(new BigDecimal("30.00")))
                    .createdDate(LocalDateTime.now())
                    .build();

            Book deletedBook = Book.builder()
                    .title("Deleted Book")
                    .author("Author B")
                    .isbn(ISBN.of("2222222222222"))
                    .price(Money.of(new BigDecimal("25.00")))
                    .createdDate(LocalDateTime.now())
                    .deletedDate(LocalDateTime.now())
                    .build();

            entityManager.persistAndFlush(activeBook);
            entityManager.persistAndFlush(deletedBook);
            entityManager.clear();

            // When
            List<Book> deletedBooks = bookRepository.findByDeletedDateIsNotNull();

            // Then
            assertThat(deletedBooks).hasSize(1);
            assertThat(deletedBooks.get(0).getTitle()).isEqualTo("Deleted Book");
        }
    }

    @Nested
    @DisplayName("편의 메서드 테스트")
    class ConvenienceMethodTest {

        @Test
        @DisplayName("findBookById 편의 메서드")
        void findBookById_존재하는ID_도서반환() {
            // Given
            Book persistedBook = entityManager.persistAndFlush(sampleBook);
            entityManager.clear();

            // When
            Book foundBook = bookRepository.findBookById(persistedBook.getId());

            // Then
            assertThat(foundBook).isNotNull();
            assertThat(foundBook.getTitle()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("findActiveBooks 편의 메서드")
        void findActiveBooks_활성도서만_반환() {
            // Given
            Book activeBook = Book.builder()
                    .title("Active Book")
                    .author("Author A")
                    .isbn(ISBN.of("1111111111111"))
                    .price(Money.of(new BigDecimal("30.00")))
                    .createdDate(LocalDateTime.now())
                    .build();

            Book deletedBook = Book.builder()
                    .title("Deleted Book")
                    .author("Author B")
                    .isbn(ISBN.of("2222222222222"))
                    .price(Money.of(new BigDecimal("25.00")))
                    .createdDate(LocalDateTime.now())
                    .deletedDate(LocalDateTime.now())
                    .build();

            entityManager.persistAndFlush(activeBook);
            entityManager.persistAndFlush(deletedBook);
            entityManager.clear();

            // When
            List<Book> activeBooks = bookRepository.findActiveBooks();

            // Then
            assertThat(activeBooks).hasSize(1);
            assertThat(activeBooks.get(0).getTitle()).isEqualTo("Active Book");
        }

        @Test
        @DisplayName("searchBooks 편의 메서드")
        void searchBooks_키워드검색_해당도서들반환() {
            // Given
            Book javaBook = Book.builder()
                    .title("Java Programming")
                    .author("John Doe")
                    .isbn(ISBN.of("1111111111111"))
                    .price(Money.of(new BigDecimal("30.00")))
                    .createdDate(LocalDateTime.now())
                    .build();

            Book springBook = Book.builder()
                    .title("Web Development")
                    .author("Spring Master")
                    .isbn(ISBN.of("2222222222222"))
                    .price(Money.of(new BigDecimal("25.00")))
                    .createdDate(LocalDateTime.now())
                    .build();

            entityManager.persistAndFlush(javaBook);
            entityManager.persistAndFlush(springBook);
            entityManager.clear();

            // When
            List<Book> books = bookRepository.searchBooks("Spring");

            // Then
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getAuthor()).isEqualTo("Spring Master");
        }
    }
}