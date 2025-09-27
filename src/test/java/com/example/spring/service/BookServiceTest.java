package com.example.spring.service;

import com.example.spring.dto.request.CreateBookRequest;
import com.example.spring.dto.request.UpdateBookRequest;
import com.example.spring.dto.response.BookResponse;
import com.example.spring.entity.Book;
import com.example.spring.exception.BookException;
import com.example.spring.exception.EntityNotFoundException;
import com.example.spring.repository.BookRepository;
import com.example.spring.service.impl.BookServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@Slf4j
@DataJpaTest
@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private Book savedBook;
    private CreateBookRequest createBookRequest;
    private UpdateBookRequest updateBookRequest;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("45.99"))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();

        savedBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("45.99"))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();

        createBookRequest = CreateBookRequest.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("45.99"))
                .available(true)
                .build();

        updateBookRequest = UpdateBookRequest.builder()
                .title("Clean Code - Updated")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("47.99"))
                .available(true)
                .build();
    }

    @Nested
    @DisplayName("도서 생성")
    class CreateBookTest {

        @Test
        @DisplayName("정상적인 도서 생성")
        void createBook_유효한도서_생성성공() {
            // Given
            given(bookRepository.existsByIsbn(createBookRequest.getIsbn())).willReturn(false);
            given(bookRepository.save(any(Book.class))).willReturn(savedBook);

            // When
            BookResponse result = bookService.createBook(createBookRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Clean Code");
            assertThat(result.getIsbn()).isEqualTo("9780132350884");

            verify(bookRepository).existsByIsbn(createBookRequest.getIsbn());
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("중복 ISBN으로 도서 생성 실패")
        void createBook_중복ISBN_예외발생() {
            // Given
            given(bookRepository.existsByIsbn(createBookRequest.getIsbn())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(createBookRequest))
                    .isInstanceOf(BookException.DuplicateIsbnException.class)
                    .hasMessageContaining("이미 존재하는 ISBN입니다");

            verify(bookRepository).existsByIsbn(createBookRequest.getIsbn());
            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("제목이 null인 도서 생성 실패")
        void createBook_제목null_예외발생() {
            // Given
            CreateBookRequest invalidRequest = CreateBookRequest.builder()
                    .title(null)
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("45.99"))
                    .available(true)
                    .build();

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(invalidRequest))
                    .isInstanceOf(BookException.InvalidBookDataException.class)
                    .hasMessageContaining("도서 제목은 필수입니다");
        }

        @Test
        @DisplayName("저자가 null인 도서 생성 실패")
        void createBook_저자null_예외발생() {
            // Given
            CreateBookRequest invalidRequest = CreateBookRequest.builder()
                    .title("Clean Code")
                    .author(null)
                    .isbn("9780132350884")
                    .price(new BigDecimal("45.99"))
                    .available(true)
                    .build();

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(invalidRequest))
                    .isInstanceOf(BookException.InvalidBookDataException.class)
                    .hasMessageContaining("저자는 필수입니다");
        }

        @Test
        @DisplayName("ISBN이 null인 도서 생성 실패")
        void createBook_ISBNnull_예외발생() {
            // Given
            CreateBookRequest invalidRequest = CreateBookRequest.builder()
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn(null)
                    .price(new BigDecimal("45.99"))
                    .available(true)
                    .build();

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(invalidRequest))
                    .isInstanceOf(BookException.InvalidBookDataException.class)
                    .hasMessageContaining("ISBN은 필수입니다");
        }

        @Test
        @DisplayName("가격이 음수인 도서 생성 실패")
        void createBook_음수가격_예외발생() {
            // Given
            CreateBookRequest invalidRequest = CreateBookRequest.builder()
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("-10.00"))
                    .available(true)
                    .build();

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(invalidRequest))
                    .isInstanceOf(BookException.InvalidBookDataException.class)
                    .hasMessageContaining("가격은 0 이상이어야 합니다");
        }
    }

    @Nested
    @DisplayName("도서 조회")
    class GetBookTest {

        @Test
        @DisplayName("ID로 활성 도서 조회 성공")
        void getBookById_활성도서_조회성공() {
            // Given
            given(bookRepository.findById(1L)).willReturn(Optional.of(savedBook));

            // When
            Optional<Book> result = bookService.getBookById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getTitle()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("ID로 삭제된 도서 조회 시 빈 Optional 반환")
        void getBookById_삭제된도서_빈Optional반환() {
            // Given
            Book deletedBook = Book.builder()
                    .id(1L)
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("45.99"))
                    .available(true)
                    .createdDate(LocalDateTime.now())
                    .deletedDate(LocalDateTime.now())
                    .build();

            given(bookRepository.findById(1L)).willReturn(Optional.of(deletedBook));

            // When
            Optional<Book> result = bookService.getBookById(1L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 도서 조회 시 빈 Optional 반환")
        void getBookById_존재하지않는ID_빈Optional반환() {
            // Given
            given(bookRepository.findById(999L)).willReturn(Optional.empty());

            // When
            Optional<Book> result = bookService.getBookById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("ISBN으로 도서 조회 성공")
        void getBookByIsbn_존재하는ISBN_조회성공() {
            // Given
            given(bookRepository.findByISBN("9780132350884")).willReturn(Optional.of(savedBook));

            // When
            Optional<Book> result = bookService.getBookByIsbn("9780132350884");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getIsbn()).isEqualTo("9780132350884");
        }

        @Test
        @DisplayName("모든 활성 도서 페이징 조회")
        void getAllActiveBooks_페이징_활성도서페이지반환() {
            // Given
            List<Book> activeBooks = List.of(savedBook,
                    Book.builder()
                            .id(2L)
                            .title("Effective Java")
                            .author("Joshua Bloch")
                            .isbn("9780134685991")
                            .price(new BigDecimal("52.99"))
                            .available(true)
                            .createdDate(LocalDateTime.now())
                            .build(),
                    Book.builder()
                            .id(3L)
                            .title("Spring in Action")
                            .author("Craig Walls")
                            .isbn("9781617294945")
                            .price(new BigDecimal("39.99"))
                            .available(true)
                            .createdDate(LocalDateTime.now())
                            .build());

            given(bookRepository.findByDeletedDateIsNull()).willReturn(activeBooks);
            Pageable pageable = PageRequest.of(0, 2);

            // When
            Page<Book> result = bookService.getAllActiveBooks(pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getContent()).extracting(Book::getTitle)
                    .containsExactly("Clean Code", "Effective Java");
        }

        @Test
        @DisplayName("빈 페이지 조회 - 범위 초과")
        void getAllActiveBooks_페이지범위초과_빈페이지반환() {
            // Given
            List<Book> activeBooks = List.of(savedBook);
            given(bookRepository.findByDeletedDateIsNull()).willReturn(activeBooks);
            Pageable pageable = PageRequest.of(1, 10); // 두 번째 페이지, 사이즈 10

            // When
            Page<Book> result = bookService.getAllActiveBooks(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumber()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("도서 수정")
    class UpdateBookTest {

        @Test
        @DisplayName("도서 정보 수정 성공")
        void updateBook_유효한정보_수정성공() {
            // Given
            Book expectedSavedBook = Book.builder()
                    .id(1L)
                    .title("Clean Code - Updated")
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("47.99"))
                    .available(true)
                    .createdDate(LocalDateTime.now())
                    .updatedDate(LocalDateTime.now())
                    .build();

            given(bookRepository.findById(1L)).willReturn(Optional.of(savedBook));
            given(bookRepository.save(any(Book.class))).willReturn(expectedSavedBook);

            // When
            BookResponse result = bookService.updateBook(1L, updateBookRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Clean Code - Updated");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("47.99"));
            assertThat(result.getAvailable()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 도서 수정 시 예외 발생")
        void updateBook_존재하지않는도서_예외발생() {
            // Given
            given(bookRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookService.updateBook(999L, updateBookRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("도서를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("삭제된 도서 수정 시 예외 발생")
        void updateBook_삭제된도서_예외발생() {
            // Given
            Book deletedBook = Book.builder()
                    .id(1L)
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("45.99"))
                    .available(true)
                    .createdDate(LocalDateTime.now())
                    .deletedDate(LocalDateTime.now())
                    .build();

            given(bookRepository.findById(1L)).willReturn(Optional.of(deletedBook));

            // When & Then
            assertThatThrownBy(() -> bookService.updateBook(1L, updateBookRequest))
                    .isInstanceOf(BookException.DeletedBookAccessException.class)
                    .hasMessageContaining("삭제된 도서는 수정할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("도서 삭제")
    class DeleteBookTest {

        @Test
        @DisplayName("도서 삭제 성공")
        void deleteBook_존재하는도서_삭제성공() {
            // Given
            given(bookRepository.findById(1L)).willReturn(Optional.of(savedBook));
            given(bookRepository.save(any(Book.class))).willReturn(savedBook);

            // When
            bookService.deleteBook(1L);

            // Then
            verify(bookRepository).findById(1L);
            verify(bookRepository).save(savedBook);
        }

        @Test
        @DisplayName("존재하지 않는 도서 삭제 시 예외 발생")
        void deleteBook_존재하지않는도서_예외발생() {
            // Given
            given(bookRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookService.deleteBook(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("도서를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("이미 삭제된 도서 삭제 시 예외 발생")
        void deleteBook_이미삭제된도서_예외발생() {
            // Given
            Book deletedBook = Book.builder()
                    .id(1L)
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("45.99"))
                    .available(true)
                    .createdDate(LocalDateTime.now())
                    .deletedDate(LocalDateTime.now())
                    .build();

            given(bookRepository.findById(1L)).willReturn(Optional.of(deletedBook));

            // When & Then
            assertThatThrownBy(() -> bookService.deleteBook(1L))
                    .isInstanceOf(BookException.DeletedBookAccessException.class)
                    .hasMessageContaining("이미 삭제된 도서입니다");
        }
    }

    @Nested
    @DisplayName("도서 복원")
    class RestoreBookTest {

        @Test
        @DisplayName("삭제된 도서 복원 성공")
        void restoreBook_삭제된도서_복원성공() {
            // Given
            Book deletedBook = Book.builder()
                    .id(1L)
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("45.99"))
                    .available(true)
                    .createdDate(LocalDateTime.now())
                    .deletedDate(LocalDateTime.now())
                    .build();

            given(bookRepository.findById(1L)).willReturn(Optional.of(deletedBook));
            given(bookRepository.save(any(Book.class))).willReturn(deletedBook);

            // When
            bookService.restoreBook(1L);

            // Then
            verify(bookRepository).findById(1L);
            verify(bookRepository).save(deletedBook);
        }

        @Test
        @DisplayName("활성 도서 복원 시 예외 발생")
        void restoreBook_활성도서_예외발생() {
            // Given
            given(bookRepository.findById(1L)).willReturn(Optional.of(savedBook));

            // When & Then
            assertThatThrownBy(() -> bookService.restoreBook(1L))
                    .isInstanceOf(BookException.InvalidBookStateException.class)
                    .hasMessageContaining("삭제되지 않은 도서는 복원할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("도서 검색")
    class SearchBookTest {

        @Test
        @DisplayName("제목으로 검색 성공")
        void searchByTitle_유효한제목_검색성공() {
            // Given
            List<Book> books = List.of(savedBook);
            given(bookRepository.findByTitleContaining("Clean")).willReturn(books);

            // When
            List<Book> result = bookService.searchByTitle("Clean");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).contains("Clean");
        }

        @Test
        @DisplayName("저자로 검색 성공")
        void searchByAuthor_유효한저자_검색성공() {
            // Given
            List<Book> books = List.of(savedBook);
            given(bookRepository.findByAuthorContaining("Martin")).willReturn(books);

            // When
            List<Book> result = bookService.searchByAuthor("Martin");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAuthor()).contains("Martin");
        }

        @Test
        @DisplayName("키워드로 검색 성공")
        void searchByKeyword_유효한키워드_검색성공() {
            // Given
            List<Book> books = List.of(savedBook);
            given(bookRepository.findByTitleContainingOrAuthorContaining("Clean", "Clean")).willReturn(books);

            // When
            List<Book> result = bookService.searchByKeyword("Clean");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).contains("Clean");
        }

        @Test
        @DisplayName("가격 범위로 검색 성공")
        void searchByPriceRange_유효한범위_검색성공() {
            // Given
            BigDecimal minPrice = new BigDecimal("40.00");
            BigDecimal maxPrice = new BigDecimal("50.00");
            List<Book> books = List.of(savedBook);
            given(bookRepository.findByPriceBetween(minPrice, maxPrice)).willReturn(books);

            // When
            List<Book> result = bookService.searchByPriceRange(minPrice, maxPrice);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPrice()).isBetween(minPrice, maxPrice);
        }

        @Test
        @DisplayName("잘못된 가격 범위로 검색 시 예외 발생")
        void searchByPriceRange_잘못된범위_예외발생() {
            // Given
            BigDecimal minPrice = new BigDecimal("50.00");
            BigDecimal maxPrice = new BigDecimal("40.00");

            // When & Then
            assertThatThrownBy(() -> bookService.searchByPriceRange(minPrice, maxPrice))
                    .isInstanceOf(BookException.InvalidPriceRangeException.class)
                    .hasMessageContaining("최소 가격이 최대 가격보다 클 수 없습니다");
        }

        @Test
        @DisplayName("복합 조건으로 도서 검색 - 페이징")
        void searchBooksWithFilters_복합조건_페이징검색성공() {
            // Given
            List<Book> allActiveBooks = List.of(
                    savedBook, // Clean Code, Robert C. Martin, 45.99
                    Book.builder()
                            .id(2L)
                            .title("Effective Java")
                            .author("Joshua Bloch")
                            .isbn("9780134685991")
                            .price(new BigDecimal("52.99"))
                            .available(true)
                            .createdDate(LocalDateTime.now())
                            .build(),
                    Book.builder()
                            .id(3L)
                            .title("Clean Architecture")
                            .author("Robert C. Martin")
                            .isbn("9780134494166")
                            .price(new BigDecimal("48.99"))
                            .available(true)
                            .createdDate(LocalDateTime.now())
                            .build()
            );

            given(bookRepository.findByDeletedDateIsNull()).willReturn(allActiveBooks);
            Pageable pageable = PageRequest.of(0, 2);

            // When - "Clean"이 포함된 제목, Martin 저자, 40-50 가격 범위
            Page<Book> result = bookService.searchBooksWithFilters(
                    "Clean", "Martin", new BigDecimal("40.00"), new BigDecimal("50.00"), true, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);  // Clean Code, Clean Architecture
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getContent()).extracting(Book::getTitle)
                    .containsExactly("Clean Code", "Clean Architecture");
        }

        @Test
        @DisplayName("복합 조건 검색 - 조건에 맞는 결과 없음")
        void searchBooksWithFilters_조건불일치_빈페이지반환() {
            // Given
            List<Book> allActiveBooks = List.of(savedBook);
            given(bookRepository.findByDeletedDateIsNull()).willReturn(allActiveBooks);
            Pageable pageable = PageRequest.of(0, 10);

            // When - 존재하지 않는 저자로 검색
            Page<Book> result = bookService.searchBooksWithFilters(
                    null, "NonExistentAuthor", null, null, null, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("재고 관리")
    class AvailabilityTest {

        @Test
        @DisplayName("재고 상태별 도서 조회")
        void getBooksByAvailability_재고상태_조회성공() {
            // Given
            List<Book> availableBooks = List.of(savedBook);
            given(bookRepository.findByAvailableTrue(true)).willReturn(availableBooks);

            // When
            List<Book> result = bookService.getBooksByAvailability(true);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAvailable()).isTrue();
        }

        @Test
        @DisplayName("도서 재고 상태 업데이트 성공")
        void updateBookAvailability_유효한상태_업데이트성공() {
            // Given
            given(bookRepository.findById(1L)).willReturn(Optional.of(savedBook));
            given(bookRepository.save(any(Book.class))).willReturn(savedBook);

            // When
            Book result = bookService.updateBookAvailability(1L, false);

            // Then
            assertThat(result).isNotNull();
            verify(bookRepository).save(savedBook);
        }
    }

    @Nested
    @DisplayName("통계")
    class StatisticsTest {

        @Test
        @DisplayName("전체 도서 수 조회")
        void getTotalBooksCount_전체도서수_반환() {
            // Given
            List<Book> allBooks = List.of(savedBook,
                    Book.builder().id(2L).title("Test").author("Test").isbn("123").price(BigDecimal.TEN).build());
            given(bookRepository.findAll()).willReturn(allBooks);

            // When
            long count = bookService.getTotalBooksCount();

            // Then
            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("활성 도서 수 조회")
        void getActiveBooksCount_활성도서수_반환() {
            // Given
            List<Book> activeBooks = List.of(savedBook);
            given(bookRepository.findByDeletedDateIsNull()).willReturn(activeBooks);

            // When
            long count = bookService.getActiveBooksCount();

            // Then
            assertThat(count).isEqualTo(1L);
        }

        @Test
        @DisplayName("ISBN 존재 여부 확인")
        void isIsbnExists_존재하는ISBN_true반환() {
            // Given
            given(bookRepository.existsByIsbn("9780132350884")).willReturn(true);

            // When
            boolean exists = bookService.isIsbnExists("9780132350884");

            // Then
            assertThat(exists).isTrue();
        }
    }
}
