package com.example.spring.controller;

import com.example.spring.dto.request.CreateMemberRequest;
import com.example.spring.dto.response.MemberResponse;
import com.example.spring.entity.MembershipType;
import com.example.spring.exception.DuplicateEmailException;
import com.example.spring.exception.MemberNotFoundException;
import com.example.spring.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 1. @WebMvcTest로 변경하고, 테스트할 컨트롤러를 지정합니다.
@WebMvcTest(MemberController.class)
class MemberControllerTest2 {

    // 2. MockMvc와 ObjectMapper는 스프링이 자동으로 설정해주므로 @Autowired로 주입받습니다.
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 3. @Mock 대신 @MockBean을 사용합니다.
    //    스프링 컨테이너에 진짜 MemberService 대신 가짜(Mock) MemberService를 등록합니다.
    @MockBean
    private MemberService memberService;

    // 4. @BeforeEach(setUp) 메서드는 더 이상 필요 없습니다.
    //    MockMvc, ObjectMapper, ControllerAdvice 모두 스프링이 자동으로 설정해줍니다.

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

        // When & Then - 이 부분은 이전 코드와 완전히 동일합니다.
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