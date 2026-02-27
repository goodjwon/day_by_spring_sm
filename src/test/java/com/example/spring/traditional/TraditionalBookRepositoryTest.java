package com.example.spring.traditional;

import com.example.spring.domain.model.Book;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("H2 schema issues - to be fixed")
@DisplayName("TraditionalBookRepository 테스트")
class TraditionalBookRepositoryTest {

    // 테스트 데이터 상수
    private static final Long EXISTING_BOOK_ID = 1L;
    private static final Long NON_EXISTENT_BOOK_ID = 999L;

    private TraditionalBookRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TraditionalBookRepository();
    }

    @AfterEach
    void tearDown() {
        closeRepository();
    }

    private void closeRepository() {
        if (repository != null) {
            repository.close();
        }
    }

    @Nested
    @DisplayName("도서 ID로 조회")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 도서 조회 성공")
        void findById_존재하는도서_성공() {
            // When
            Book book = repository.findById(EXISTING_BOOK_ID);

            // Then
            assertNotNull(book);
            assertEquals(EXISTING_BOOK_ID, book.getId());
        }

        @Test
        @DisplayName("존재하지 않는 도서 조회 - null 반환")
        void findById_존재하지않는도서_null반환() {
            // When
            Book book = repository.findById(NON_EXISTENT_BOOK_ID);

            // Then
            assertNull(book);
        }
    }

    @Nested
    @DisplayName("전체 도서 조회")
    class FindAllTest {

        @Test
        @DisplayName("전체 도서 조회 성공")
        void findAll_전체조회_성공() {
            // When
            List<Book> books = repository.findAll();

            // Then
            assertNotNull(books);
            assertFalse(books.isEmpty());
        }
    }
}