package com.example.spring.controller;

import com.example.spring.dto.request.CreateMemberRequest;
import com.example.spring.dto.response.MemberResponse;
import com.example.spring.entity.MembershipType;
import com.example.spring.exception.DuplicateEmailException;
import com.example.spring.exception.GlobalExceptionHandler;
import com.example.spring.exception.MemberNotFoundException;
import com.example.spring.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MemberController 테스트 (추천 방식)
 *
 * JPA 의존성 없이 순수 Controller 테스트
 * - @ExtendWith(MockitoExtension.class): 빠른 Mockito 테스트
 * - Spring Context 로드 없어서 JPA 메타모델 오류 방지
 * - 테스트 실행 속도 향상 (0.5-1초)
 */
@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // MockMvc 수동 설정 (JPA 의존성 완전 제거)
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // ObjectMapper 설정
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("회원 가입 성공")
    void createMember_Success() throws Exception {
        // Given
        CreateMemberRequest request = CreateMemberRequest.builder()
                .name("홍길동")
                .email("hong@example.com")
                .membershipType(MembershipType.REGULAR)
                .build();

        MemberResponse response = MemberResponse.builder()
                .id(1L)
                .name("홍길동")
                .email("hong@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();

        given(memberService.createMember(any(CreateMemberRequest.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("hong@example.com"))
                .andExpect(jsonPath("$.membershipType").value("REGULAR"));
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 중복")
    void createMember_DuplicateEmail() throws Exception {
        // Given
        CreateMemberRequest request = CreateMemberRequest.builder()
                .name("홍길동")
                .email("duplicate@example.com")
                .membershipType(MembershipType.REGULAR)
                .build();

        given(memberService.createMember(any(CreateMemberRequest.class)))
                .willThrow(new DuplicateEmailException("duplicate@example.com"));

        // When & Then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다: duplicate@example.com"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("회원 조회 성공")
    void findMemberById_Success() throws Exception {
        // Given
        Long memberId = 1L;
        MemberResponse response = MemberResponse.builder()
                .id(memberId)
                .name("홍길동")
                .email("hong@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();

        given(memberService.findMemberById(memberId))
                .willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/members/{id}", memberId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(memberId))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("hong@example.com"))
                .andExpect(jsonPath("$.membershipType").value("REGULAR"));
    }

    @Test
    @DisplayName("회원 조회 실패 - 존재하지 않는 회원")
    void findMemberById_NotFound() throws Exception {
        // Given
        Long memberId = 999L;
        given(memberService.findMemberById(memberId))
                .willThrow(new MemberNotFoundException(memberId));

        // When & Then
        mockMvc.perform(get("/api/v1/members/{id}", memberId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("MEMBER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다. ID: " + memberId))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}