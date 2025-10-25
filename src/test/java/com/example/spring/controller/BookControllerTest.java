package com.example.spring.controller;

import com.example.spring.dto.request.CreateBookRequest;
import com.example.spring.dto.request.UpdateBookRequest;
import com.example.spring.dto.response.BookResponse;
import com.example.spring.entity.Book;
import com.example.spring.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
public class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;
    private Book testBook;
    private BookResponse testResponse;
    private CreateBookRequest createBookRequest;
    private UpdateBookRequest updateBookRequest;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("45000"))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();

        testResponse = BookResponse.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("45000"))
                .available(true)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        createBookRequest = CreateBookRequest.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("45000"))
                .available(true)
                .build();

        updateBookRequest = UpdateBookRequest.builder()
                .title("Clean Code - Updated")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("47000"))
                .available(true)
                .build();
    }

    @Nested
    @DisplayName("도서 생성")
    class CreateBookTest {

        @Test
        @DisplayName("도서 생성 - 성공")
        void createBook_유효한요청_생성성공() throws Exception {
            //given
            given(bookService.createBook(any(CreateBookRequest.class)))
                    .willReturn(testResponse);

            // when & then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testResponse)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Clean Code"))
                    .andExpect(jsonPath("$.isbn").value("9780132350884"));

            verify(bookService).createBook(any(CreateBookRequest.class));
        }

        @Test
        @DisplayName("제목 누락 시 400 에러")
        void createBook_제목이_누락됨() throws Exception {
            // Given
            CreateBookRequest invalidRequest = CreateBookRequest.builder()
                    .title(null)
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("45000"))
                    .available(true)
                    .build();
            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            then(bookService).should(never()).createBook(any(CreateBookRequest.class));
        }

        @Test
        @DisplayName("ISBN 누락 시 400에러 발생")
        void createBook_잘못된ISBN_400에러() throws Exception {
            //Given
            CreateBookRequest invalidRequest = CreateBookRequest.builder()
                    .title(null)
                    .author("Robert C. Martin")
                    .isbn("97801323508Cd")
                    .price(new BigDecimal("45000"))
                    .available(true)
                    .build();
            //When&Then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            then(bookService).should(never()).createBook(any(CreateBookRequest.class));
        }

    }

    @Nested
    @DisplayName("도서 조회")
    class GetBookTest {

        @Test
        @DisplayName("도서 상세 조회 성공")
        void getBook_존재하는ID_조회성공() throws Exception {
            //Given
            Long bookId = 1L;
            BookResponse response = BookResponse.builder()
                    .id(bookId)
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("45000"))
                    .available(true)
                    .build();
            given(bookService.getBookById(bookId)).willReturn(response);
            //When&Then
            mockMvc.perform(get("/api/v1/books/{id}", bookId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Clean Code"))
                    .andExpect(jsonPath("$.author").value("Robert C. Martin"));

            verify(bookService).getBookById(bookId);
        }

        @Test
        @DisplayName("존재하지 않는 도서 조회 시 500 에러")
        void getBook_존재하지않는ID_500에러() throws Exception {
            //Given
            Long bookId = 1L;
            given(bookService.getBookById(bookId))
                    .willThrow(new RuntimeException("도서 조회에 실패 하였습니다"));
            //When&Then
            mockMvc.perform(get("/api/v1/books/{id}", bookId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());

            verify(bookService).getBookById(bookId);
        }

        @Test
        @DisplayName("ISBN으로 도서 조회 성공")
        void getBookByIsbn_존재하는ISBN_조회성공() throws Exception {
            //Given
            Book testBook1 = testBook;
            given(bookService.getBookByIsbn(testBook1.getIsbn())).willReturn(Optional.of(testBook1));

            //When&Then
            mockMvc.perform(get("/api/v1/books/isbn/{isbn}", testBook1.getIsbn())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.isbn").value(testBook1.getIsbn()));

            verify(bookService).getBookByIsbn(testBook1.getIsbn());
        }

        @Test
        @DisplayName("ISBN으로 도서 조회 실패 - 404에러")
        void getBookByIsbn_존재하는않는ISBN_404에러() throws Exception {
            //Given
            String bookIsbn = "0000000000000";
            given(bookService.getBookByIsbn(bookIsbn)).willReturn(Optional.empty());
            //When&Then
            mockMvc.perform(get("/api/v1/books/isbn/{isbn}", bookIsbn)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(bookService).getBookByIsbn(bookIsbn);
        }

        @Test
        @DisplayName("활성 도서 목록 조회 (페이징)")
        void getAllActiveBooks_페이징_조회성공() throws Exception {
            //Given
            int page = 0;
            int size = 10;
            String sort = "createdDate";
            String direction = "desc";

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));

            List<Book> content = List.of(
                    Book.builder().id(1L).title("Book 1").author("Author A").isbn("111").build(),
                    Book.builder().id(2L).title("Book 2").author("Author B").isbn("222").build()
            );

            long totalElements = 20L;
            Page<Book> bookPage = new PageImpl<>(content, pageable, totalElements);

            given(bookService.getAllActiveBooks(pageable)).willReturn(bookPage);

            // When&Then
            mockMvc.perform(get("/api/v1/books")
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size))
                            .param("sort", sort)
                            .param("direction", direction)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(content.size()))
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].title").value("Book 1"))
                    .andExpect(jsonPath("$.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.number").value(page))
                    .andExpect(jsonPath("$.size").value(size));

            verify(bookService).getAllActiveBooks(pageable);
        }
    }

    @Nested
    @DisplayName("도서 수정")
    class UpdateBookTest {

        @Test
        @DisplayName("도서 수정 성공")
        void updateBook_유효한요청_수정성공() throws Exception {
            //Given
            Long bookId = 1L;
            given(bookService.updateBook(eq(bookId), any(UpdateBookRequest.class))).willReturn(testResponse);
            //When&Then
            mockMvc.perform(put("/api/v1/books/{id}", bookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testResponse)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(bookId))
                    .andExpect(jsonPath("$.title").value("Clean Code"))
                    .andExpect(jsonPath("$.price").value(45000));

            verify(bookService).updateBook(eq(bookId), any(UpdateBookRequest.class));
        }

        @Test
        @DisplayName("도서 삭제 성공")
        void deleteBook_존재하는ID_삭제성공() throws Exception {
            //Given
            Long bookId = 1L;
            willDoNothing().given(bookService).deleteBook(bookId);
            //When&Then
            mockMvc.perform(delete("/api/v1/books/{id}", bookId))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(bookService).deleteBook(bookId);
        }

        @Test
        @DisplayName("도서 복원 성공")
        void restoreBook_유요한요청_복원성공() throws Exception {
            //Given
            Long bookId = 1L;
            willDoNothing().given(bookService).restoreBook(bookId);
            //When&Then
            mockMvc.perform(patch("/api/v1/books/{id}/restore", bookId))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(bookService).restoreBook(bookId);
        }
    }

    @Nested
    @DisplayName("도서 검색")
    class SearchBookTest {

        @Test
        @DisplayName("제목으로 검색 성공")
        void searchByTitle_유효한제목_검색성공() throws Exception {
            //Given
            String searchTitle = "Clean";
            List<Book> bookList = List.of(testBook);
            given(bookService.searchByTitle(searchTitle)).willReturn(bookList);
            //When&Then
            mockMvc.perform(get("/api/v1/books/search/title")
                    .param("title", searchTitle)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].title").value("Clean Code"));

            verify(bookService).searchByTitle(searchTitle);
        }

        @Test
        @DisplayName("저자로 검색 성공")
        void searchByAuthor_유효한저자_검색성공() throws Exception {
            //Given
            String searchAuthor = "Martin";
            List<Book> searchBook = List.of(testBook);
            given(bookService.searchByAuthor(searchAuthor)).willReturn(searchBook);
            //When&Then
            mockMvc.perform(get("/api/v1/books/search/author")
                            .param("author", searchAuthor)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].author").value("Robert C. Martin"));

            verify(bookService).searchByAuthor(searchAuthor);
        }
    }

    @Nested
    @DisplayName("재고 관리")
    class AvailabilityTest {

        @Test
        @DisplayName("재고 상태 업데이트 성공")
        void updateBookAvailability_유효한상태_업데이트성공() throws Exception {
            //Given
            Long bookId = 1L;
            boolean newStatus = false;
            Book updateBook = Book.builder()
                    .id(bookId)
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("45000"))
                    .available(newStatus)
                    .build();
            given(bookService.updateBookAvailability(bookId, newStatus)).willReturn(updateBook);
            //When&Then
            mockMvc.perform(patch("/api/v1/books/{id}/availability", bookId)
                            .param("available", String.valueOf(newStatus))
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateBook)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available").value(false));

            verify(bookService).updateBookAvailability(bookId, newStatus);
        }

        @Test
        @DisplayName("재고 상태별 도서 조회")
        void getBooksByAvailability_재고상태_조회성공() throws Exception {
            //Given
            boolean status = true;
            List<Book> bookList = List.of(
                    Book.builder().id(1L).title("Book 1").available(true).build(),
                    Book.builder().id(2L).title("Book 2").available(false).build()
            );
            given(bookService.getBooksByAvailability(status)).willReturn(bookList);
            //When&Then
            mockMvc.perform(get("/api/v1/books/availability/{available}", status)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].available").value(status));

            verify(bookService).getBooksByAvailability(status);
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
            // When & Then
            mockMvc.perform(patch("/api/books/1/restore"))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(bookService).restoreBook(1L);
        }
    }
}
