package com.example.spring.repository;

import com.example.spring.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(BookRepository.class)
public class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private Book sampleBook;

    // 각 테스트 실행 전 샘플 데이터 초기화
    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("45.99"))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();
        entityManager.persist(sampleBook);
    }

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class BasicCrudTest {

        @Test
        @DisplayName("ID로 도서 조회 성공")
        public void findById_존재하는도서_도서반환() {
            // When
            Optional<Book> foundBook = bookRepository.findById(sampleBook.getId());

            // Then
            assertThat(foundBook).isPresent();
            assertThat(foundBook.get().getTitle()).isEqualTo("Clean Code");
            assertThat(foundBook.get().getAuthor()).isEqualTo("Robert C. Martin");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
        public void findById_존재하지않는도서_빈Optional반환() {
            // Given
            Long nonExistentId = 9999L;

            // When
            Optional<Book> foundBook = bookRepository.findById(nonExistentId);

            // Then
            assertThat(foundBook).isEmpty();
        }

        @Test
        @DisplayName("모든 도서 조회")
        public void findAll_도서목록반환() {
            // When
            List<Book> books = bookRepository.findAll();

            // Then
            assertThat(books).isNotEmpty();
            assertThat(books).contains(sampleBook);
        }

        @Test
        @DisplayName("도서 저장 성공")
        public void save_신규도서_저장성공() {
            // Given
            Book newBook = Book.builder()
                    .author("새 저자")
                    .title("새 도서")
                    .price(new BigDecimal("10000"))
                    .isbn("1234567890" + System.currentTimeMillis())
                    .available(true)
                    .createdDate(LocalDateTime.now())
                    .build();

            // When
            Book savedBook = bookRepository.save(newBook);
            entityManager.flush();

            // Then
            assertThat(savedBook.getId()).isNotNull();
            assertThat(bookRepository.findById(savedBook.getId())).isPresent();
        }

        @Test
        @DisplayName("도서 삭제 성공")
        public void deleteById_도서삭제() {
            // Given
            Long bookId = sampleBook.getId();

            // When
            bookRepository.deleteById(bookId);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<Book> deletedBook = bookRepository.findById(bookId);
            assertThat(deletedBook).isEmpty();
        }
    }

    @Nested
    @DisplayName("편의 메서드 테스트")
    class ConvenienceMethodTest {
        @Test
        @DisplayName("findBookById 편의 메서드 - 존재하는 경우")
        public void findBookById_존재하는도서_도서직접반환() {
            // When
            Book foundBook = bookRepository.findByIdOrThrow(sampleBook.getId());

            // Then
            assertThat(foundBook).isNotNull();
            assertThat(foundBook.getId()).isEqualTo(sampleBook.getId());
        }

        @Test
        @DisplayName("findBookById 편의 메서드 - 존재하지 않는 경우")
        public void findBookById_존재하지않는도서_null반환() {
            // Given
            Long nonExistentId = 9999L;

            // When
            Book foundBook = bookRepository.findByIdOrThrow(nonExistentId);

            // Then
            assertThat(foundBook).isNull();
        }

        @Test
        @DisplayName("findActiveBooks 편의 메서드 (삭제되지 않은 도서 조회)")
        void findActiveBooks_삭제되지않은도서만조회() {
            // Given: Book 엔티티에 deletedDate가 있다고 가정

            // When
            List<Book> activeBooks = bookRepository.findByDeletedDateIsNull();

            // Then
            assertThat(activeBooks).isNotEmpty();
            assertThat(activeBooks).allMatch(book -> book.getDeletedDate() == null);
        }
    }

    @Nested
    @DisplayName("Soft Delete 테스트")
    class SoftDeleteTest {

        @Test
        @DisplayName("삭제된 도서만 조회")
        void findByDeletedDateIsNotNull_삭제된도서만조회() {
            // Given
            sampleBook.setDeletedDate(LocalDateTime.now());
            entityManager.persistAndFlush(sampleBook);

            // When
            List<Book> deletedBooks = bookRepository.findByDeletedDateIsNotNull();

            // Then
            assertThat(deletedBooks).isNotEmpty();
            assertThat(deletedBooks).contains(sampleBook);
            assertThat(deletedBooks).allMatch(book -> book.getDeletedDate() != null);
        }

        @Test
        @DisplayName("삭제되지 않은 도서만 조회")
        void findByDeletedDateIsNull_삭제되지않은도서만조회() {
            // Given
            // When
            List<Book> activeBooks = bookRepository.findByDeletedDateIsNull();

            // Then
            assertThat(activeBooks).isNotEmpty();
            assertThat(activeBooks).contains(sampleBook);
            assertThat(activeBooks).allMatch(book -> book.getDeletedDate() == null);
        }
    }

    @Nested
    @DisplayName("재고 상태별 테스트")
    class AvailabilityTest {

        private Book availableBook;
        private Book unavailableBook;

        @BeforeEach
        void setUp() {
            availableBook = Book.builder()
                    .title("Available Book")
                    .author("Author A")
                    .isbn("1111111111111")
                    .price(new BigDecimal("30.00"))
                    .available(true)
                    .createdDate(LocalDateTime.now())
                    .build();

            unavailableBook = Book.builder()
                    .title("Unavailable Book")
                    .author("Author B")
                    .isbn("2222222222222")
                    .price(new BigDecimal("25.00"))
                    .available(false)
                    .createdDate(LocalDateTime.now())
                    .build();

            entityManager.persist(availableBook);
            entityManager.persist(unavailableBook);
            entityManager.flush();
        }

        @Test
        @DisplayName("재고 상태별 도서 조회 - 재고 있음")
        void getBooksByAvailability_재고있는도서조회() {
            // When
            List<Book> availableBooks = bookRepository.findByAvailableTrue(true);

            // Then
            assertThat(availableBooks).contains(availableBook);
            assertThat(availableBooks).doesNotContain(unavailableBook);
        }

        @Test
        @DisplayName("재고 상태별 도서 조회 - 재고 없음")
        void getBooksByAvailability_재고없는도서조회() {
            // When
            List<Book> unavailableBooks = bookRepository.findByAvailableTrue(false);

            // Then
            assertThat(unavailableBooks).contains(unavailableBook);
            assertThat(unavailableBooks).doesNotContain(availableBook);
        }
    }

    @Nested
    @DisplayName("검색 기능 테스트")
    class SearchTest {

        @BeforeEach
        void setUp() {
            Book effectiveJava = Book.builder()
                    .title("Effective Java")
                    .author("Joshua Bloch")
                    .isbn("9780134685991")
                    .price(new BigDecimal("52.99"))
                    .createdDate(LocalDateTime.now())
                    .build();

            Book springInAction = Book.builder()
                    .title("Spring in Action")
                    .author("Craig Walls")
                    .isbn("9781617294945")
                    .price(new BigDecimal("39.99"))
                    .createdDate(LocalDateTime.now())
                    .build();

            entityManager.persist(effectiveJava);
            entityManager.persist(springInAction);
            entityManager.flush();
        }

        @Test
        @DisplayName("제목 또는 저자로 도서 검색")
        void searchByTitleOrAuthor() {
            // When
            List<Book> results = bookRepository.findByTitleContainingOrAuthorContaining("Code", "Bloch");

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Book::getTitle).contains("Clean Code", "Effective Java");
        }

        @Test
        @DisplayName("제목으로 도서 검색")
        void searchByTitle() {
            // When
            List<Book> results = bookRepository.findByTitleContaining("Java");

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).isEqualTo("Effective Java");
        }

        @Test
        @DisplayName("저자로 도서 검색")
        void searchByAuthor() {
            // When
            List<Book> results = bookRepository.findByAuthorContaining("Martin");

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getAuthor()).isEqualTo("Robert C. Martin");
        }

        @Test
        @DisplayName("가격 범위로 도서 검색")
        void searchByPriceRange() {
            // When
            List<Book> results = bookRepository.findByPriceBetween(new BigDecimal("40.00"), new BigDecimal("50.00"));

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
        }
    }

    @Nested
    @DisplayName("ISBN 관련 테스트")
    class IsbnTest {

        @Test
        @DisplayName("ISBN 존재 여부 확인 - 존재하는 경우")
        void isbnExists_존재하는경우_True반환() {
            // When
            boolean exists = bookRepository.existsByIsbn("9780132350884");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("ISBN 존재 여부 확인 - 존재하지 않는 경우")
        void isbnExists_존재하지않는경우_False반환() {
            // When
            boolean exists = bookRepository.existsByIsbn("0000000000000");

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("ISBN으로 도서 조회 성공")
        void findByIsbn_존재하는ISBN_도서반환() {
            // When
            Optional<Book> foundBook = bookRepository.findByISBN("9780132350884");

            // Then
            assertThat(foundBook).isPresent();
            assertThat(foundBook.get().getTitle()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("존재하지 않는 ISBN으로 조회 시 빈 Optional 반환")
        void findByIsbn_존재하지않는ISBN_빈Optional반환() {
            // When
            Optional<Book> foundBook = bookRepository.findByISBN("0000000000000");

            // Then
            assertThat(foundBook).isEmpty();
        }
    }
}