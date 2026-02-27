package com.example.spring.presentation.controller;

import com.example.spring.application.MemberService;
import com.example.spring.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminMemberController 테스트")
class AdminMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    @DisplayName("관리자 승격 성공")
    void promoteToAdmin_Success() throws Exception {
        mockMvc.perform(put("/api/admin/members/{id}/promote", 1L))
                .andExpect(status().isOk());

        then(memberService).should().promoteToAdmin(1L);
    }

    @Test
    @DisplayName("존재하지 않는 회원 승격 시 404")
    void promoteToAdmin_NotFound() throws Exception {
        doThrow(new MemberException.MemberNotFoundException(999L))
                .when(memberService).promoteToAdmin(999L);

        mockMvc.perform(put("/api/admin/members/{id}/promote", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("MEMBER_NOT_FOUND"));
    }
}