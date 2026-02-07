package com.example.spring.controller;

import com.example.spring.dto.request.CreateMemberRequest;
import com.example.spring.dto.request.UpdateMemberRequest;
import com.example.spring.dto.response.MemberResponse;
import com.example.spring.entity.MembershipType;
import com.example.spring.exception.DuplicateEmailException;
import com.example.spring.exception.MemberException;
import com.example.spring.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 1. @WebMvcTest로 변경하고, 테스트할 컨트롤러를 지정합니다.
@WebMvcTest(MemberController.class)
class MemberControllerTest {

    // 2. MockMvc와 ObjectMapper는 스프링이 자동으로 설정해주므로 @Autowired로 주입받습니다.
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 3. @Mock 대신 @MockBean을 사용합니다.
    //    스프링 컨테이너에 진짜 MemberService 대신 가짜(Mock) MemberService를 등록합니다.
    @MockitoBean
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
    @DisplayName("회원 가입 실패 - 검증 오류")
    void createMember_ValidationError() throws Exception {
        // Given: 잘못된 요청값 (빈 이름, 잘못된 이메일)
        String invalidJson = "{\n" +
                "  \"name\": \"\",\n" +
                "  \"email\": \"invalid-email\"\n" +
                "}";

        // When & Then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='name')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='email')]").exists());
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
                .willThrow(new MemberException.MemberNotFoundException(memberId));

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

    @Test
    @DisplayName("회원 수정 성공")
    void updateMember_Success() throws Exception {
        // Given
        Long memberId = 1L;
        UpdateMemberRequest request = UpdateMemberRequest.builder()
                .name("임꺽정")
                .email("lim@example.com")
                .build();

        MemberResponse response = MemberResponse.builder()
                .id(memberId)
                .name("임꺽정")
                .email("lim@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.of(2023, 5, 1, 12, 0))
                .build();

        given(memberService.updateMember(eq(memberId), any(UpdateMemberRequest.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(memberId))
                .andExpect(jsonPath("$.name").value("임꺽정"))
                .andExpect(jsonPath("$.email").value("lim@example.com"));
    }

    @Test
    @DisplayName("회원 수정 실패 - 검증 오류")
    void updateMember_ValidationError() throws Exception {
        // Given: 잘못된 요청값 (이름 너무 짧음, 이메일 형식 오류)
        String invalidJson = "{\n" +
                "  \"name\": \"a\",\n" +
                "  \"email\": \"bad-email\"\n" +
                "}";

        // When & Then
        mockMvc.perform(put("/api/v1/members/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("회원 수정 실패 - 이메일 중복")
    void updateMember_DuplicateEmail() throws Exception {
        // Given
        Long memberId = 1L;
        UpdateMemberRequest request = UpdateMemberRequest.builder()
                .name("임꺽정")
                .email("dup@example.com")
                .build();

        given(memberService.updateMember(eq(memberId), any(UpdateMemberRequest.class)))
                .willThrow(new DuplicateEmailException("dup@example.com"));

        // When & Then
        mockMvc.perform(put("/api/v1/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다: dup@example.com"));
    }

    @Test
    @DisplayName("회원 목록 조회 성공")
    void findAllMembers_Success() throws Exception {
        // Given
        List<MemberResponse> list = Arrays.asList(
                MemberResponse.builder().id(1L).name("A").email("a@example.com").membershipType(MembershipType.REGULAR).build(),
                MemberResponse.builder().id(2L).name("B").email("b@example.com").membershipType(MembershipType.PREMIUM).build()
        );
        given(memberService.findAllMembers()).willReturn(list);

        // When & Then
        mockMvc.perform(get("/api/v1/members/all").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].membershipType").value("PREMIUM"));
    }

    @Test
    @DisplayName("회원 목록 조회 - 빈 목록 반환")
    void findAllMembers_Empty() throws Exception {
        // Given
        given(memberService.findAllMembers()).willReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/members/all").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("이름으로 회원 검색 성공")
    void findMembersByName_Success() throws Exception {
        // Given
        List<MemberResponse> list = Collections.singletonList(
                MemberResponse.builder().id(10L).name("홍길동").email("hong@example.com").membershipType(MembershipType.REGULAR).build()
        );
        given(memberService.findMembersByName("홍")).willReturn(list);

        // When & Then
        mockMvc.perform(get("/api/v1/members/search").param("name", "홍"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("홍길동"));
    }

    @Test
    @DisplayName("이름으로 회원 검색 - 결과 없음")
    void findMembersByName_Empty() throws Exception {
        // Given
        given(memberService.findMembersByName("zzz")).willReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/members/search").param("name", "zzz"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("멤버십 타입별 회원 조회 성공")
    void getMembersByMembershipType_Success() throws Exception {
        // Given
        List<MemberResponse> list = Arrays.asList(
                MemberResponse.builder().id(3L).name("C").email("c@example.com").membershipType(MembershipType.REGULAR).build(),
                MemberResponse.builder().id(4L).name("D").email("d@example.com").membershipType(MembershipType.REGULAR).build()
        );
        given(memberService.findMembersByMembershipType(MembershipType.REGULAR)).willReturn(list);

        // When & Then
        mockMvc.perform(get("/api/v1/members/membership/{type}", MembershipType.REGULAR))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].membershipType").value("REGULAR"))
                .andExpect(jsonPath("$[1].membershipType").value("REGULAR"));
    }
}