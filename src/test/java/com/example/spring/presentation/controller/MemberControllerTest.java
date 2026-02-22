package com.example.spring.presentation.controller;

import com.example.spring.application.dto.request.CreateMemberRequest;
import com.example.spring.application.dto.request.UpdateMemberRequest;
import com.example.spring.application.dto.response.MemberLoanLimitInfo;
import com.example.spring.application.dto.response.MemberResponse;
import com.example.spring.domain.model.MembershipType;
import com.example.spring.exception.MemberException;
import com.example.spring.exception.MembershipUpgradeException;
import com.example.spring.application.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MemberController 계층형 테스트")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @Nested
    @DisplayName("회원 생성")
    class CreateMemberTest {
        @Test
        @DisplayName("정상 생성")
        void createMember_Success() throws Exception {
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

            mockMvc.perform(post("/api/members")
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
        @DisplayName("이메일 중복으로 실패")
        void createMember_DuplicateEmail() throws Exception {
            CreateMemberRequest request = CreateMemberRequest.builder()
                    .name("홍길동")
                    .email("duplicate@example.com")
                    .membershipType(MembershipType.REGULAR)
                    .build();

            given(memberService.createMember(any(CreateMemberRequest.class)))
                    .willThrow(new MemberException.DuplicateEmailException("duplicate@example.com"));

            mockMvc.perform(post("/api/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"))
                    .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다: duplicate@example.com"))
                    .andExpect(jsonPath("$.timestamp").exists());

        }

        @Test
        @DisplayName("검증 오류")
        void createMember_ValidationError() throws Exception {
            String invalidJson = "{\n" +
                    "  \"name\": \"\",\n" +
                    "  \"email\": \"invalid-email\"\n" +
                    "}";

            mockMvc.perform(post("/api/members")
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
    }

    @Nested
    @DisplayName("회원 조회")
    class FindMemberTest {
        @Test
        @DisplayName("ID로 조회 성공")
        void findMemberById_Success() throws Exception {
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

            mockMvc.perform(get("/api/members/{id}", memberId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(memberId))
                    .andExpect(jsonPath("$.name").value("홍길동"))
                    .andExpect(jsonPath("$.email").value("hong@example.com"))
                    .andExpect(jsonPath("$.membershipType").value("REGULAR"));
        }

        @Test
        @DisplayName("존재하지 않는 회원")
        void findMemberById_NotFound() throws Exception {
            Long memberId = 999L;
            given(memberService.findMemberById(memberId))
                    .willThrow(new MemberException.MemberNotFoundException(memberId));

            mockMvc.perform(get("/api/members/{id}", memberId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("MEMBER_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다. ID: " + memberId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("이름으로 검색")
        void findMembersByName_Success() throws Exception {
            List<MemberResponse> list = Collections.singletonList(
                    MemberResponse.builder().id(10L).name("홍길동").email("hong@example.com").membershipType(MembershipType.REGULAR).build()
            );
            given(memberService.findMembersByName("홍")).willReturn(list);

            mockMvc.perform(get("/api/members/search").param("name", "홍"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("홍길동"));
        }

        @Test
        @DisplayName("이름 검색 결과 없음")
        void findMembersByName_Empty() throws Exception {
            given(memberService.findMembersByName("zzz")).willReturn(Collections.emptyList());

            mockMvc.perform(get("/api/members/search").param("name", "zzz"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        @DisplayName("멤버십 타입별 조회")
        void getMembersByMembershipType_Success() throws Exception {
            List<MemberResponse> list = Arrays.asList(
                    MemberResponse.builder().id(3L).name("C").email("c@example.com").membershipType(MembershipType.REGULAR).build(),
                    MemberResponse.builder().id(4L).name("D").email("d@example.com").membershipType(MembershipType.REGULAR).build()
            );
            given(memberService.findMembersByMembershipType(MembershipType.REGULAR)).willReturn(list);

            mockMvc.perform(get("/api/members/membership/{type}", MembershipType.REGULAR))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].membershipType").value("REGULAR"))
                    .andExpect(jsonPath("$[1].membershipType").value("REGULAR"));
        }
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class UpdateMemberTest {
        @Test
        @DisplayName("수정 성공")
        void updateMember_Success() throws Exception {
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

            mockMvc.perform(put("/api/members/{id}", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(memberId))
                    .andExpect(jsonPath("$.name").value("임꺽정"))
                    .andExpect(jsonPath("$.email").value("lim@example.com"));
        }

        @Test
        @DisplayName("검증 오류")
        void updateMember_ValidationError() throws Exception {
            String invalidJson = "{\n" +
                    "  \"name\": \"a\",\n" +
                    "  \"email\": \"bad-email\"\n" +
                    "}";

            mockMvc.perform(put("/api/members/{id}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다"))
                    .andExpect(jsonPath("$.fieldErrors").isArray());
        }

        @Test
        @DisplayName("이메일 중복")
        void updateMember_DuplicateEmail() throws Exception {
            Long memberId = 1L;
            UpdateMemberRequest request = UpdateMemberRequest.builder()
                    .name("임꺽정")
                    .email("dup@example.com")
                    .build();

            given(memberService.updateMember(eq(memberId), any(UpdateMemberRequest.class)))
                    .willThrow(new MemberException.DuplicateEmailException("dup@example.com"));

            mockMvc.perform(put("/api/members/{id}", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"))
                    .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다: dup@example.com"));
        }
    }

    @Nested
    @DisplayName("회원 삭제")
    class DeleteMemberTest {
        @Test
        @DisplayName("삭제 성공")
        void deleteMember_Success() throws Exception {
            Long id = 1L;
            mockMvc.perform(delete("/api/members/{id}", id))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("존재하지 않는 회원 삭제")
        void deleteMember_NotFound() throws Exception {
            Long id = 999L;
            willThrow(new MemberException.MemberNotFoundException(id)).given(memberService).deleteMember(id);

            mockMvc.perform(delete("/api/members/{id}", id))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("MEMBER_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("멤버십 업그레이드")
    class UpgradeMembershipTest {
        @Test
        @DisplayName("REGULAR -> PREMIUM 업그레이드 성공")
        void upgradeMembership_Success() throws Exception {
            mockMvc.perform(put("/api/members/{id}/membership", 1)
                            .param("membershipType", "PREMIUM"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("업그레이드 실패 - 정지 회원")
        void upgradeMembership_Suspended_Fail() throws Exception {
            willThrow(new MembershipUpgradeException("정지 회원은 업그레이드할 수 없습니다.")).given(memberService).upgradeMembership(1L, MembershipType.PREMIUM);

            mockMvc.perform(put("/api/members/{id}/membership", 1)
                            .param("membershipType", "PREMIUM"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("MEMBERSHIP_UPGRADE_ERROR"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("업그레이드 실패 - 동일 타입")
        void upgradeMembership_SameType_Fail() throws Exception {
            willThrow(new MembershipUpgradeException("동일한 타입으로 업그레이드할 수 없습니다.")).given(memberService).upgradeMembership(1L, MembershipType.REGULAR);

            mockMvc.perform(put("/api/members/{id}/membership", 1)
                            .param("membershipType", "REGULAR"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("MEMBERSHIP_UPGRADE_ERROR"));
        }
    }

    @Nested
    @DisplayName("이메일 검증")
    class EmailValidationTest {
        @Test
        @DisplayName("사용 가능한 이메일")
        void validateEmail_Available() throws Exception {
            given(memberService.validateEmailDuplicate("new@example.com")).willReturn(true);

            mockMvc.perform(get("/api/members/email/validate").param("email", "new@example.com"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @Test
        @DisplayName("중복된 이메일")
        void validateEmail_Duplicate() throws Exception {
            given(memberService.validateEmailDuplicate("existing@example.com")).willReturn(false);

            mockMvc.perform(get("/api/members/email/validate").param("email", "existing@example.com"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }
    }

    @Nested
    @DisplayName("대여 제한 정보")
    class LoanLimitTest {
        @Test
        @DisplayName("REGULAR 회원 대여 제한 정보")
        void getMemberLoanLimitInfo_Regular() throws Exception {
            MemberLoanLimitInfo info = MemberLoanLimitInfo.builder()
                    .memberId(1L)
                    .memberName("홍길동")
                    .membershipType(MembershipType.REGULAR)
                    .maxLoanCount(5)
                    .currentLoanCount(0)
                    .remainingLoanCount(5)
                    .canLoan(true)
                    .build();
            given(memberService.getMemberLoanLimitInfo(1L)).willReturn(info);

            mockMvc.perform(get("/api/members/{id}/loan-limit", 1))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(1))
                    .andExpect(jsonPath("$.membershipType").value("REGULAR"))
                    .andExpect(jsonPath("$.canLoan").value(true));
        }
    }

    @Nested
    @DisplayName("전체 회원 조회")
    class FindAllMembersTest {
        @Test
        @DisplayName("페이지네이션 조회")
        void findAllMembers_WithPagination_Success() throws Exception {
            List<MemberResponse> list = Arrays.asList(
                    MemberResponse.builder().id(1L).name("A").email("a@example.com").membershipType(MembershipType.REGULAR).build()
            );
            org.springframework.data.domain.PageImpl<MemberResponse> page = new org.springframework.data.domain.PageImpl<>(list);
            given(memberService.findAllMembers(any(org.springframework.data.domain.Pageable.class))).willReturn(page);

            mockMvc.perform(get("/api/members").param("page", "0").param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L));
        }

        @Test
        @DisplayName("빈 페이지")
        void findAllMembers_Empty() throws Exception {
            org.springframework.data.domain.PageImpl<MemberResponse> emptyPage = new org.springframework.data.domain.PageImpl<>(Collections.emptyList());
            given(memberService.findAllMembers(any(org.springframework.data.domain.Pageable.class))).willReturn(emptyPage);

            mockMvc.perform(get("/api/members"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }
}