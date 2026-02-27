package com.example.spring.presentation.controller;

import com.example.spring.application.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;

    @PutMapping("/{id}/promote")
    public ResponseEntity<Void> promoteToAdmin(@PathVariable Long id) {
        log.info("관리자 승격 요청 - 회원ID: {}", id);
        memberService.promoteToAdmin(id);
        return ResponseEntity.ok().build();
    }
}