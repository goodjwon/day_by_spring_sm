package com.example.spring.controller;

import com.example.spring.dto.request.CreateLoanRequest;
import com.example.spring.dto.request.UpdateLoanRequest;
import com.example.spring.dto.response.LoanResponse;
import com.example.spring.entity.LoanStatus;
import com.example.spring.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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

@WebMvcTest(LoanController.class)
@ActiveProfiles("test")
@DisplayName("LoanController 통합 테스트")
public class LoanControllerTeat {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService;

    private LoanResponse testLoanResponse;
    private CreateLoanRequest createLoanRequest;
    private UpdateLoanRequest updateLoanRequest;

    @BeforeEach
    void setUp() {
        testLoanResponse = LoanResponse.builder()
                .id(1L)
                .memberId(1L)
                .memberName("홍길동")
                .memberEmail("hong@example.com")
                .bookId(1L)
                .bookTitle("Clean Code")
                .bookAuthor("Robert C. Martin")
                .bookIsbn("9780132350884")
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .status(LoanStatus.ACTIVE)
                .overdueFee(BigDecimal.ZERO)
                .isOverdue(false)
                .overdueDays(0L)
                .createdDate(LocalDateTime.now())
                .build();

        createLoanRequest = CreateLoanRequest.builder()
                .memberId(1L)
                .bookId(1L)
                .loanDays(14)
                .build();

        updateLoanRequest = UpdateLoanRequest.builder()
                .status(LoanStatus.RETURNED)
                .build();
    }

    @Nested
    @DisplayName("[API 명세 #1] 전체 대출 목록 조회 (페이징)")
    class GetAllLoansWithPaginationTest {

        @Test
        @DisplayName("GET /api/admin/loans - 페이징 조회 성공")
        void getAllLoans_페이징조회_200응답() throws Exception {
            // Given
            Page<LoanResponse> page = new PageImpl<>(
                    List.of(testLoanResponse),
                    PageRequest.of(0, 10),
                    1
            );
            given(loanService.getAllLoansWithPagination(any(), any(), any())).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/admin/loans")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sortKey", "loanDate")
                            .param("sortOrder", "desc"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(loanService).getAllLoansWithPagination(any(), any(), any());
        }

        @Test
        @DisplayName("GET /api/admin/loans - 검색 필터 적용")
        void getAllLoans_검색필터_200응답() throws Exception {
            // Given
            Page<LoanResponse> page = new PageImpl<>(List.of(testLoanResponse));
            given(loanService.getAllLoansWithPagination(any(), eq("홍길동"), eq("ACTIVE")))
                    .willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/admin/loans")
                            .param("searchQuery", "홍길동")
                            .param("statusFilter", "ACTIVE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].memberName").value("홍길동"));

            verify(loanService).getAllLoansWithPagination(any(), eq("홍길동"), eq("ACTIVE"));
        }

        @Test
        @DisplayName("GET /api/admin/loans - 오름차순 정렬")
        void getAllLoans_오름차순정렬_200응답() throws Exception {
            // Given
            Page<LoanResponse> page = new PageImpl<>(
                    List.of(testLoanResponse),
                    PageRequest.of(0, 10),
                    1
            );
            given(loanService.getAllLoansWithPagination(any(), any(), any())).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/admin/loans")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sortKey", "loanDate")
                            .param("sortOrder", "asc"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            verify(loanService).getAllLoansWithPagination(any(), any(), any());
        }

        @Test
        @DisplayName("GET /api/admin/loans - 다양한 정렬 키 (dueDate)")
        void getAllLoans_다양한정렬키_200응답() throws Exception {
            // Given
            Page<LoanResponse> page = new PageImpl<>(
                    List.of(testLoanResponse),
                    PageRequest.of(0, 10),
                    1
            );
            given(loanService.getAllLoansWithPagination(any(), any(), any())).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/admin/loans")
                            .param("sortKey", "dueDate")
                            .param("sortOrder", "desc"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            verify(loanService).getAllLoansWithPagination(any(), any(), any());
        }

        @Test
        @DisplayName("GET /api/admin/loans - 기본 파라미터 (파라미터 없음)")
        void getAllLoans_기본파라미터_200응답() throws Exception {
            // Given
            Page<LoanResponse> page = new PageImpl<>(
                    List.of(testLoanResponse),
                    PageRequest.of(0, 20), // 기본값
                    1
            );
            given(loanService.getAllLoansWithPagination(any(), any(), any())).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/admin/loans"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            verify(loanService).getAllLoansWithPagination(any(), any(), any());
        }

        @Test
        @DisplayName("GET /api/admin/loans - 빈 결과")
        void getAllLoans_빈결과_200응답() throws Exception {
            // Given
            Page<LoanResponse> emptyPage = new PageImpl<>(
                    List.of(),
                    PageRequest.of(0, 10),
                    0
            );
            given(loanService.getAllLoansWithPagination(any(), any(), any())).willReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/admin/loans")
                            .param("searchQuery", "존재하지않는검색어"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(loanService).getAllLoansWithPagination(any(), eq("존재하지않는검색어"), any());
        }

        @Test
        @DisplayName("GET /api/admin/loans - 다양한 상태 필터 (OVERDUE)")
        void getAllLoans_상태필터OVERDUE_200응답() throws Exception {
            // Given
            Page<LoanResponse> page = new PageImpl<>(List.of(testLoanResponse));
            given(loanService.getAllLoansWithPagination(any(), any(), eq("OVERDUE")))
                    .willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/admin/loans")
                            .param("statusFilter", "OVERDUE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            verify(loanService).getAllLoansWithPagination(any(), any(), eq("OVERDUE"));
        }

        @Test
        @DisplayName("GET /api/admin/loans - 다양한 상태 필터 (RETURNED)")
        void getAllLoans_상태필터RETURNED_200응답() throws Exception {
            // Given
            Page<LoanResponse> page = new PageImpl<>(List.of(testLoanResponse));
            given(loanService.getAllLoansWithPagination(any(), any(), eq("RETURNED")))
                    .willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/admin/loans")
                            .param("statusFilter", "RETURNED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            verify(loanService).getAllLoansWithPagination(any(), any(), eq("RETURNED"));
        }

        @Test
        @DisplayName("GET /api/admin/loans - 복합 필터 (검색어 + 상태 + 정렬)")
        void getAllLoans_복합필터_200응답() throws Exception {
            // Given
            Page<LoanResponse> page = new PageImpl<>(List.of(testLoanResponse));
            given(loanService.getAllLoansWithPagination(any(), eq("Clean"), eq("ACTIVE")))
                    .willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/admin/loans")
                            .param("searchQuery", "Clean")
                            .param("statusFilter", "ACTIVE")
                            .param("sortKey", "loanDate")
                            .param("sortOrder", "desc"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            verify(loanService).getAllLoansWithPagination(any(), eq("Clean"), eq("ACTIVE"));
        }

        @Test
        @DisplayName("GET /api/admin/loans - 큰 페이지 번호")
        void getAllLoans_큰페이지번호_200응답() throws Exception {
            // Given
            Page<LoanResponse> page = new PageImpl<>(
                    List.of(),
                    PageRequest.of(10, 10), // 11번째 페이지
                    0
            );
            given(loanService.getAllLoansWithPagination(any(), any(), any())).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/admin/loans")
                            .param("page", "10")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());

            verify(loanService).getAllLoansWithPagination(any(), any(), any());
        }

        @Test
        @DisplayName("GET /api/admin/loans - 커스텀 페이지 크기")
        void getAllLoans_커스텀페이지크기_200응답() throws Exception {
            // Given
            Page<LoanResponse> page = new PageImpl<>(
                    List.of(testLoanResponse),
                    PageRequest.of(0, 5),
                    1
            );
            given(loanService.getAllLoansWithPagination(any(), any(), any())).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/admin/loans")
                            .param("page", "0")
                            .param("size", "5"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size").value(5));

            verify(loanService).getAllLoansWithPagination(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("[API 명세 #2] 대출 상세 조회")
    class GetLoanByIdTest {

        @Test
        @DisplayName("GET /api/admin/loans/{id} - 조회 성공")
        void getLoan_존재하는대출_200응답() throws Exception {
            // Given
            given(loanService.getLoanById(1L)).willReturn(Optional.of(testLoanResponse));

            // When & Then
            mockMvc.perform(get("/api/admin/loans/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.memberName").value("홍길동"))
                    .andExpect(jsonPath("$.bookTitle").value("Clean Code"));

            verify(loanService).getLoanById(1L);
        }
    }

    @Nested
    @DisplayName("[API 명세 #3] 대출 생성")
    class CreateLoanTest {

        @Test
        @DisplayName("POST /api/admin/loans - 대출 생성 성공")
        void createLoan_유효한요청_201응답() throws Exception {
            // Given
            given(loanService.createLoan(any(CreateLoanRequest.class))).willReturn(testLoanResponse);

            // When & Then
            mockMvc.perform(post("/api/admin/loans")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createLoanRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.memberId").value(1))
                    .andExpect(jsonPath("$.bookId").value(1))
                    .andExpect(jsonPath("$.memberName").value("홍길동"))
                    .andExpect(jsonPath("$.bookTitle").value("Clean Code"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(loanService).createLoan(any(CreateLoanRequest.class));
        }

        @Test
        @DisplayName("POST /api/admin/loans - 필수 필드 누락 시 400 응답")
        void createLoan_필수필드누락_400응답() throws Exception {
            // Given
            CreateLoanRequest invalidRequest = CreateLoanRequest.builder()
                    .memberId(null)  // memberId 누락
                    .bookId(1L)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/admin/loans")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("[API 명세 #4] 대출 정보 수정")
    class UpdateLoanTest {

        @Test
        @DisplayName("PATCH /api/admin/loans/{id} - 반납 처리 성공")
        void updateLoan_반납처리_200응답() throws Exception {
            // Given
            LoanResponse returnedLoan = LoanResponse.builder()
                    .id(1L)
                    .memberId(1L)
                    .memberName("홍길동")
                    .bookId(1L)
                    .bookTitle("Clean Code")
                    .loanDate(LocalDateTime.now().minusDays(10))
                    .dueDate(LocalDateTime.now().plusDays(4))
                    .returnDate(LocalDateTime.now())
                    .status(LoanStatus.RETURNED)
                    .overdueFee(BigDecimal.ZERO)
                    .build();

            given(loanService.updateLoan(eq(1L), any(UpdateLoanRequest.class))).willReturn(returnedLoan);

            // When & Then
            mockMvc.perform(patch("/api/admin/loans/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateLoanRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("RETURNED"))
                    .andExpect(jsonPath("$.returnDate").exists());

            verify(loanService).updateLoan(eq(1L), any(UpdateLoanRequest.class));
        }

        @Test
        @DisplayName("PATCH /api/admin/loans/{id} - 날짜 연장 성공")
        void updateLoan_날짜연장_200응답() throws Exception {
            // Given
            UpdateLoanRequest extendRequest = UpdateLoanRequest.builder()
                    .dueDate(LocalDateTime.now().plusDays(21))
                    .build();

            LoanResponse extendedLoan = LoanResponse.builder()
                    .id(1L)
                    .memberId(1L)
                    .bookId(1L)
                    .dueDate(LocalDateTime.now().plusDays(21))
                    .status(LoanStatus.ACTIVE)
                    .build();

            given(loanService.updateLoan(eq(1L), any(UpdateLoanRequest.class))).willReturn(extendedLoan);

            // When & Then
            mockMvc.perform(patch("/api/admin/loans/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(extendRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(loanService).updateLoan(eq(1L), any(UpdateLoanRequest.class));
        }
    }

    @Nested
    @DisplayName("[API 명세 #5] 대출 기록 삭제")
    class DeleteLoanTest {

        @Test
        @DisplayName("DELETE /api/admin/loans/{id} - 삭제 성공")
        void deleteLoan_정상삭제_204응답() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/admin/loans/1"))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(loanService).deleteLoan(1L);
        }
    }

    // ========================================
    // 사용자 API 테스트
    // ========================================

    @Nested
    @DisplayName("[API 명세 Client #1] 내 대출 목록 조회")
    class GetMyLoansTest {

        @Test
        @DisplayName("GET /api/client/loans - 내 대출 목록 조회 성공")
        void getMyLoans_목록조회_200응답() throws Exception {
            // Given
            given(loanService.getMyLoans(1L, "ALL")).willReturn(List.of(testLoanResponse));

            // When & Then
            mockMvc.perform(get("/api/client/loans")
                            .param("memberId", "1")
                            .param("statusFilter", "ALL"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].memberId").value(1));

            verify(loanService).getMyLoans(1L, "ALL");
        }

        @Test
        @DisplayName("GET /api/client/loans - 상태 필터 적용")
        void getMyLoans_상태필터_200응답() throws Exception {
            // Given
            given(loanService.getMyLoans(1L, "ACTIVE")).willReturn(List.of(testLoanResponse));

            // When & Then
            mockMvc.perform(get("/api/client/loans")
                            .param("memberId", "1")
                            .param("statusFilter", "ACTIVE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("ACTIVE"));

            verify(loanService).getMyLoans(1L, "ACTIVE");
        }
    }

    @Nested
    @DisplayName("[API 명세 Client #2] 도서 반납 신청")
    class ReturnBookByMemberTest {

        @Test
        @DisplayName("POST /api/client/loans/{id}/return - 반납 신청 성공")
        void returnBookByMember_정상반납_200응답() throws Exception {
            // Given
            LoanResponse returnedLoan = LoanResponse.builder()
                    .id(1L)
                    .memberId(1L)
                    .memberName("홍길동")
                    .bookId(1L)
                    .bookTitle("Clean Code")
                    .returnDate(LocalDateTime.now())
                    .status(LoanStatus.RETURNED)
                    .overdueFee(BigDecimal.ZERO)
                    .build();

            given(loanService.returnBookByMember(1L, 1L)).willReturn(returnedLoan);

            // When & Then
            mockMvc.perform(post("/api/client/loans/1/return")
                            .param("memberId", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("RETURNED"))
                    .andExpect(jsonPath("$.returnDate").exists());

            verify(loanService).returnBookByMember(1L, 1L);
        }
    }

    // ========================================
    // 추가 기능 테스트
    // ========================================

    @Nested
    @DisplayName("추가 기능 - 연체/활성 대출 조회")
    class AdditionalFeaturesTest {

        @Test
        @DisplayName("GET /api/admin/loans/overdue - 연체된 대출 목록 조회")
        void getOverdueLoans_연체목록_200응답() throws Exception {
            // Given
            LoanResponse overdueLoan = LoanResponse.builder()
                    .id(2L)
                    .status(LoanStatus.OVERDUE)
                    .isOverdue(true)
                    .overdueDays(7L)
                    .overdueFee(new BigDecimal("7000"))
                    .build();

            given(loanService.getOverdueLoans()).willReturn(List.of(overdueLoan));

            // When & Then
            mockMvc.perform(get("/api/admin/loans/overdue"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].isOverdue").value(true));

            verify(loanService).getOverdueLoans();
        }

        @Test
        @DisplayName("GET /api/admin/loans/active - 현재 대여 중인 목록 조회")
        void getActiveLoans_대여중인목록_200응답() throws Exception {
            // Given
            given(loanService.getActiveLoans()).willReturn(List.of(testLoanResponse));

            // When & Then
            mockMvc.perform(get("/api/admin/loans/active"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("ACTIVE"));

            verify(loanService).getActiveLoans();
        }

        @Test
        @DisplayName("GET /api/admin/loans/member/{memberId} - 회원별 대여 내역 조회")
        void getLoansByMember_회원ID로조회_200응답() throws Exception {
            // Given
            given(loanService.getLoansByMemberId(1L)).willReturn(List.of(testLoanResponse));

            // When & Then
            mockMvc.perform(get("/api/admin/loans/member/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].memberId").value(1));

            verify(loanService).getLoansByMemberId(1L);
        }

        @Test
        @DisplayName("GET /api/admin/loans/book/{bookId} - 도서별 대여 내역 조회")
        void getLoansByBook_도서ID로조회_200응답() throws Exception {
            // Given
            given(loanService.getLoansByBookId(1L)).willReturn(List.of(testLoanResponse));

            // When & Then
            mockMvc.perform(get("/api/admin/loans/book/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].bookId").value(1));

            verify(loanService).getLoansByBookId(1L);
        }
    }

    @Nested
    @DisplayName("추가 기능 - JOIN 활용 검색")
    class JoinSearchTest {

        @Test
        @DisplayName("GET /api/admin/loans/search/by-member-name - 회원 이름으로 검색")
        void getLoansByMemberName_회원이름검색_200응답() throws Exception {
            // Given
            given(loanService.getLoansByMemberName("홍길동")).willReturn(List.of(testLoanResponse));

            // When & Then
            mockMvc.perform(get("/api/admin/loans/search/by-member-name")
                            .param("name", "홍길동"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].memberName").value("홍길동"));

            verify(loanService).getLoansByMemberName("홍길동");
        }

        @Test
        @DisplayName("GET /api/admin/loans/search/by-book-title - 도서 제목으로 검색")
        void getLoansByBookTitle_도서제목검색_200응답() throws Exception {
            // Given
            given(loanService.getLoansByBookTitle("Clean Code")).willReturn(List.of(testLoanResponse));

            // When & Then
            mockMvc.perform(get("/api/admin/loans/search/by-book-title")
                            .param("title", "Clean Code"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].bookTitle").value("Clean Code"));

            verify(loanService).getLoansByBookTitle("Clean Code");
        }

        @Test
        @DisplayName("GET /api/admin/loans/with-details - N+1 최적화 조회")
        void getAllLoansWithDetails_상세조회_200응답() throws Exception {
            // Given
            given(loanService.getAllLoansWithDetails()).willReturn(List.of(testLoanResponse));

            // When & Then
            mockMvc.perform(get("/api/admin/loans/with-details"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].memberName").exists())
                    .andExpect(jsonPath("$[0].bookTitle").exists());

            verify(loanService).getAllLoansWithDetails();
        }

        @Test
        @DisplayName("GET /api/admin/loans/member/{memberId}/borrowed-books - 회원의 대여 중인 도서")
        void getCurrentlyBorrowedBooksByMember_대여중도서_200응답() throws Exception {
            // Given
            com.example.spring.dto.response.BookResponse bookResponse =
                    com.example.spring.dto.response.BookResponse.builder()
                            .id(1L)
                            .title("Clean Code")
                            .author("Robert C. Martin")
                            .isbn("9780132350884")
                            .price(new BigDecimal("33000"))
                            .available(false)
                            .build();

            given(loanService.getCurrentlyBorrowedBooksByMember(1L))
                    .willReturn(List.of(bookResponse));

            // When & Then
            mockMvc.perform(get("/api/admin/loans/member/1/borrowed-books"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Clean Code"));

            verify(loanService).getCurrentlyBorrowedBooksByMember(1L);
        }

        @Test
        @DisplayName("GET /api/admin/loans/overdue/with-member-info - 연체 및 회원 정보 조회")
        void getOverdueLoansWithMemberInfo_연체회원정보_200응답() throws Exception {
            // Given
            LoanResponse overdueLoan = LoanResponse.builder()
                    .id(1L)
                    .memberId(1L)
                    .memberName("홍길동")
                    .memberEmail("hong@example.com")
                    .status(LoanStatus.OVERDUE)
                    .isOverdue(true)
                    .build();

            given(loanService.getOverdueLoansWithMemberInfo()).willReturn(List.of(overdueLoan));

            // When & Then
            mockMvc.perform(get("/api/admin/loans/overdue/with-member-info"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].memberName").value("홍길동"))
                    .andExpect(jsonPath("$[0].status").value("OVERDUE"));

            verify(loanService).getOverdueLoansWithMemberInfo();
        }
    }
}
