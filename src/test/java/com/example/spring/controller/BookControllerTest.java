package com.example.spring.controller;

import com.example.spring.dto.request.CreateBookRequest;
import com.example.spring.dto.request.UpdateBookRequest;
import com.example.spring.dto.response.BookResponse;
import com.example.spring.entity.Book;
import com.example.spring.service.BookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
                    .willReturn(BookResponse.from(testBook));

            // when & then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createBookRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Clean Code"))
                    .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                    .andExpect(jsonPath("$.price").value(new BigDecimal("45000")))
                    .andExpect(jsonPath("$.isbn").value("9780132350884"))
                    .andExpect(jsonPath("$.available").value(true));
        }
        @Test
        @DisplayName("제목 누락 시 400 에러")
        void createBook_제목이_누락됨() throws Exception {
            // Given
            CreateBookRequest invalidRequest = CreateBookRequest.builder()
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("45000"))
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("ISBN 누락 시 400에러 발생")
        void createBook_잘못된ISBN_400에러() throws Exception {
            //Given
            CreateBookRequest invalidIsbn = CreateBookRequest.builder()
                    .author("Robert C. Martin")
                    .isbn("9780132350883")
                    .price(new BigDecimal("45000"))
                    .build();
            //When&Then
            mockMvc.perform(post("/api/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidIsbn)))
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
            //Given
            CreateBookRequest searchBook = CreateBookRequest.builder()
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn("9780132350883")
                    .price(new BigDecimal("45000"))
                    .available(true)
                    .build();
            //When&Then
            mockMvc.perform(get("/api/v1/books/{id}", 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Clean Code"))
                    .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                    .andExpect(jsonPath("$.price").value(new BigDecimal("45000")));
        }

        @Test
        @DisplayName("존재하지 않는 도서 조회 시 500 에러")
        void getBook_존재하지않는ID_500에러() throws Exception {}

        @Test
        @DisplayName("ISBN으로 도서 조회 성공")
        void getBookByIsbn_존재하는ISBN_조회성공() throws Exception {}

        @Test
        @DisplayName("활성 도서 목록 조회 (페이징)")
        void getAllActiveBooks_페이징_조회성공() throws Exception {}


    }

    @Nested
    @DisplayName("도서 수정")
    class UpdateBookTest {
        @Test
        @DisplayName("도서 수정 성공")
        void updateBook_유효한요청_수정성공() throws Exception {}

        @Test
        @DisplayName("도서 삭제 성공")
        void deleteBook_존재하는ID_삭제성공() throws Exception {}

    }

    @Nested
    @DisplayName("도서 검색")
    class SearchBookTest {
        @Test
        @DisplayName("제목으로 검색 성공")
        void searchByTitle_유효한제목_검색성공() throws Exception {}

        @Test
        @DisplayName("저자로 검색 성공")
        void searchByAuthor_유효한저자_검색성공() throws Exception {}

    }

    @Nested
    @DisplayName("재고 관리")
    class AvailabilityTest {
        @Test
        @DisplayName("재고 상태 업데이트 성공")
        void updateBookAvailability_유효한상태_업데이트성공() throws Exception {}

    }

    @Test
    @DisplayName("재고 상태별 도서 조회")
    void getBooksByAvailability_재고상태_조회성공() throws Exception {

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
//
//        @Test
//        @DisplayName("도서 복원 성공")
//        void restoreBook_삭제된도서_복원성공() throws Exception {
//            // Given
//            given(bookService.getBookById(1L)).willReturn(Optional.of(BookResponse.from(testBook)));
//
//            // When & Then
//            mockMvc.perform(patch("/api/books/1/restore"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.id").value(1L));
//
//            verify(bookService).restoreBook(1L);
//            verify(bookService).getBookById(1L);
//        }
    }



}
