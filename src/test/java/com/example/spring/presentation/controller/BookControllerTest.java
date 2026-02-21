package com.example.spring.presentation.controller;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import com.example.spring.application.dto.request.CreateBookRequest;
import com.example.spring.application.dto.request.UpdateBookRequest;
import com.example.spring.application.dto.response.BookResponse;
import com.example.spring.domain.model.Book;
import com.example.spring.application.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BookController 통합 테스트")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private Book testBook;
    private CreateBookRequest createBookRequest;
    private UpdateBookRequest updateBookRequest;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn(ISBN.of("9780132350884"))
                .price(Money.of(new BigDecimal("45000")))
                .available(true)
                .coverImageUrl("https://cdn.example.com/books/clean-code.jpg")
                .createdDate(LocalDateTime.now())
                .build();

        createBookRequest = CreateBookRequest.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("45000"))
                .available(true)
                .coverImageUrl("https://cdn.example.com/books/clean-code.jpg")
                .build();

        updateBookRequest = UpdateBookRequest.builder()
                .title("Clean Code - Updated")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("47000"))
                .available(true)
                .coverImageUrl("https://cdn.example.com/books/clean-code-updated.jpg")
                .build();
    }

    @Nested
    @DisplayName("도서 생성")
    class CreateBookTest {

        @Test
        @DisplayName("도서 생성 성공")
        void createBook_유효한요청_생성성공() throws Exception {
            // Given
            given(bookService.createBook(any(CreateBookRequest.class))).willReturn(BookResponse.from(testBook));

            // When & Then
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createBookRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Clean Code"))
                    .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                    .andExpect(jsonPath("$.isbn").value("978-0-13-235088-4"))
                    .andExpect(jsonPath("$.price").value(45000))
                    .andExpect(jsonPath("$.available").value(true))
                    .andExpect(jsonPath("$.coverImageUrl").value("https://cdn.example.com/books/clean-code.jpg"));

            verify(bookService).createBook(any(CreateBookRequest.class));
        }

        @Test
        @DisplayName("제목 누락 시 400 에러")
        void createBook_제목누락_400에러() throws Exception {
            // Given
            CreateBookRequest invalidRequest = CreateBookRequest.builder()
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("45000"))
                    .build();

            // When & Then
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("잘못된 ISBN 형식 시 400 에러")
        void createBook_잘못된ISBN_400에러() throws Exception {
            // Given
            CreateBookRequest invalidRequest = CreateBookRequest.builder()
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn("invalid-isbn")
                    .price(new BigDecimal("45000"))
                    .build();

            // When & Then
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("도서 조회")
    class GetBookTest {

        @Test
        @DisplayName("도서 상세 조회 성공")
        void getBook_존재하는ID_조회성공() throws Exception {
            // Given
            given(bookService.getBookById(1L)).willReturn(Optional.of(BookResponse.from(testBook)));

            // When & Then
            mockMvc.perform(get("/api/books/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Clean Code"))
                    .andExpect(jsonPath("$.isbn").value("978-0-13-235088-4"));

            verify(bookService).getBookById(1L);
        }

        //todo GlobalException 처리
//        @Test
//        @DisplayName("존재하지 않는 도서 조회 시 404 에러")
//        void getBook_존재하지않는ID_404에러() throws Exception {
//            // Given
//            given(bookService.getBookById(999L)).willReturn(Optional.empty());
//
//            // When & Then
//            mockMvc.perform(get("/api/books/999"))
//                    .andDo(print())
//                    .andExpect(status().isNotFound());
//
//            verify(bookService).getBookById(999L);
//        }

        @Test
        @DisplayName("ISBN으로 도서 조회 성공")
        void getBookByIsbn_존재하는ISBN_조회성공() throws Exception {
            // Given
            given(bookService.getBookByIsbn("9780132350884")).willReturn(Optional.of(testBook));

            // When & Then
            mockMvc.perform(get("/api/books/isbn/9780132350884"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Clean Code"))
                    .andExpect(jsonPath("$.isbn").value("978-0-13-235088-4"));

            verify(bookService).getBookByIsbn("9780132350884");
        }

        @Test
        @DisplayName("활성 도서 목록 조회 (페이징)")
        void getAllActiveBooks_페이징_조회성공() throws Exception {
            // Given
            List<Book> books = List.of(testBook);
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(0, 10), 1);
            given(bookService.getAllActiveBooks(any(Pageable.class))).willReturn(bookPage);

            // When & Then
            mockMvc.perform(get("/api/books")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "createdDate")
                            .param("direction", "desc"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));

            verify(bookService).getAllActiveBooks(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("도서 수정")
    class UpdateBookTest {

        @Test
        @DisplayName("도서 수정 성공")
        void updateBook_유효한요청_수정성공() throws Exception {
            // Given
            Book updatedBook = Book.builder()
                    .id(1L)
                    .title("Clean Code - Updated")
                    .author("Robert C. Martin")
                    .isbn(ISBN.of("9780132350884"))
                    .price(Money.of(new BigDecimal("47000")))
                    .available(true)
                    .coverImageUrl("https://cdn.example.com/books/clean-code-updated.jpg")
                    .createdDate(LocalDateTime.now())
                    .updatedDate(LocalDateTime.now())
                    .build();

            given(bookService.updateBook(eq(1L), any(UpdateBookRequest.class))).willReturn(BookResponse.from(updatedBook));

            // When & Then
            mockMvc.perform(put("/api/books/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateBookRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Clean Code - Updated"))
                    .andExpect(jsonPath("$.price").value(47000))
                    .andExpect(jsonPath("$.coverImageUrl").value("https://cdn.example.com/books/clean-code-updated.jpg"));

            verify(bookService).updateBook(eq(1L), any(UpdateBookRequest.class));
        }
    }

    @Nested
    @DisplayName("도서 삭제")
    class DeleteBookTest {

        @Test
        @DisplayName("도서 삭제 성공")
        void deleteBook_존재하는ID_삭제성공() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/books/1"))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(bookService).deleteBook(1L);
        }
    }

    @Nested
    @DisplayName("도서 검색")
    class SearchBookTest {

        @Test
        @DisplayName("제목으로 검색 성공")
        void searchByTitle_유효한제목_검색성공() throws Exception {
            // Given
            List<Book> books = List.of(testBook);
            given(bookService.searchByTitle("Clean")).willReturn(books);

            // When & Then
            mockMvc.perform(get("/api/books/search/title")
                            .param("title", "Clean")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(bookService).searchByTitle("Clean");
        }

        @Test
        @DisplayName("저자로 검색 성공")
        void searchByAuthor_유효한저자_검색성공() throws Exception {
            // Given
            List<Book> books = List.of(testBook);
            given(bookService.searchByAuthor("Martin")).willReturn(books);

            // When & Then
            mockMvc.perform(get("/api/books/search/author")
                            .param("author", "Martin")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].author").value("Robert C. Martin"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(bookService).searchByAuthor("Martin");
        }

        @Test
        @DisplayName("복합 조건 검색 (Stream 버전 - 페이징)")
        void searchBooksWithFilters_복합조건_검색성공() throws Exception {
            // Given
            List<Book> books = List.of(testBook);
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(0, 10), 1);
            given(bookService.searchBooksWithFilters(
                    eq("Clean"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
            )).willReturn(bookPage);

            // When & Then
            mockMvc.perform(get("/api/books/search")
                            .param("title", "Clean")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(bookService).searchBooksWithFilters(
                    eq("Clean"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
            );
        }

        @Test
        @DisplayName("복합 조건 검색 (JPQL Query 버전 - 페이징)")
        void searchBooksWithQueryFilters_복합조건_검색성공() throws Exception {
            // Given
            List<Book> books = List.of(testBook);
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(0, 10), 1);
            given(bookService.searchBooksWithQueryFilters(
                    eq("Clean"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
            )).willReturn(bookPage);

            // When & Then
            mockMvc.perform(get("/api/books/search/query")
                            .param("title", "Clean")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(bookService).searchBooksWithQueryFilters(
                    eq("Clean"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
            );
        }
    }

    @Nested
    @DisplayName("재고 관리")
    class AvailabilityTest {

        @Test
        @DisplayName("재고 상태 업데이트 성공")
        void updateBookAvailability_유효한상태_업데이트성공() throws Exception {
            // Given
            Book updatedBook = Book.builder()
                    .id(1L)
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn(ISBN.of("9780132350884"))
                    .price(Money.of(new BigDecimal("45000")))
                    .available(false)
                    .createdDate(LocalDateTime.now())
                    .updatedDate(LocalDateTime.now())
                    .build();

            given(bookService.updateBookAvailability(1L, false)).willReturn(updatedBook);

            // When & Then
            mockMvc.perform(patch("/api/books/1/availability")
                            .param("available", "false"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available").value(false));

            verify(bookService).updateBookAvailability(1L, false);
        }

        @Test
        @DisplayName("재고 상태별 도서 조회")
        void getBooksByAvailability_재고상태_조회성공() throws Exception {
            // Given
            List<Book> availableBooks = List.of(testBook);
            given(bookService.getBooksByAvailability(true)).willReturn(availableBooks);

            // When & Then
            mockMvc.perform(get("/api/books/availability/true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].available").value(true));

            verify(bookService).getBooksByAvailability(true);
        }
    }

    @Nested
    @DisplayName("통계 및 유틸리티")
    class StatisticsTest {

        @Test
        @DisplayName("도서 통계 조회")
        void getBookStatistics_통계조회_성공() throws Exception {
            // Given
            given(bookService.getTotalBooksCount()).willReturn(10L);
            given(bookService.getActiveBooksCount()).willReturn(8L);

            // When & Then
            mockMvc.perform(get("/api/books/statistics"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalBooks").value(10))
                    .andExpect(jsonPath("$.activeBooks").value(8))
                    .andExpect(jsonPath("$.deletedBooks").value(2));

            verify(bookService).getTotalBooksCount();
            verify(bookService).getActiveBooksCount();
        }

        @Test
        @DisplayName("ISBN 중복 확인")
        void validateIsbn_중복확인_성공() throws Exception {
            // Given
            given(bookService.isIsbnExists("9780132350884")).willReturn(true);

            // When & Then
            mockMvc.perform(get("/api/books/validate/isbn")
                            .param("isbn", "9780132350884"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            verify(bookService).isIsbnExists("9780132350884");
        }
    }

    @Nested
    @DisplayName("도서 복원")
    class RestoreBookTest {

        @Test
        @DisplayName("도서 복원 성공")
        void restoreBook_삭제된도서_복원성공() throws Exception {
            // Given
            given(bookService.getBookById(1L)).willReturn(Optional.of(BookResponse.from(testBook)));

            // When & Then
            mockMvc.perform(patch("/api/books/1/restore"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));

            verify(bookService).restoreBook(1L);
            verify(bookService).getBookById(1L);
        }
    }
}