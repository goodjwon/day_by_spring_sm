package com.example.spring.presentation.controller;

import com.example.spring.application.dto.request.CreateMemberRequest;
import com.example.spring.application.dto.request.UpdateMemberRequest;
import com.example.spring.application.dto.response.MemberLoanLimitInfo;
import com.example.spring.application.dto.response.MemberResponse;
import com.example.spring.domain.model.MembershipType;
import com.example.spring.application.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 회원 관리 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 가입
     */
    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody CreateMemberRequest request) {
        log.info("회원 가입 요청 - 이메일: {}, 이름: {}", request.getEmail(), request.getName());

        MemberResponse response = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 회원 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long id) {
        log.debug("회원 조회 요청 - ID: {}", id);

        MemberResponse response = memberService.findMemberById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<MemberResponse>> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("회원 목록 조회 요청 - 페이지: {}, 크기: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<MemberResponse> responses = memberService.findAllMembers(pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * 회원 정보 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMemberRequest request) {

        log.info("회원 정보 수정 요청 - ID: {}", id);

        MemberResponse response = memberService.updateMember(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        log.info("회원 삭제 요청 - ID: {}", id);

        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 이름으로 회원 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<MemberResponse>> searchMembersByName(@RequestParam String name) {
        log.debug("이름으로 회원 검색 - 검색어: {}", name);

        List<MemberResponse> responses = memberService.findMembersByName(name);
        return ResponseEntity.ok(responses);
    }

    /**
     * 멤버십 타입별 회원 조회
     */
    @GetMapping("/membership/{type}")
    public ResponseEntity<List<MemberResponse>> getMembersByMembershipType(@PathVariable MembershipType type) {
        log.debug("멤버십 타입별 회원 조회 - 타입: {}", type);

        List<MemberResponse> responses = memberService.findMembersByMembershipType(type);
        return ResponseEntity.ok(responses);
    }

    /**
     * 멤버십 업그레이드
     */
    @PutMapping("/{id}/membership")
    public ResponseEntity<Void> upgradeMembership(
            @PathVariable Long id,
            @RequestParam MembershipType membershipType) {

        log.info("멤버십 업그레이드 요청 - 회원ID: {}, 타입: {}", id, membershipType);

        memberService.upgradeMembership(id, membershipType);
        return ResponseEntity.ok().build();
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/email/validate")
    public ResponseEntity<Boolean> validateEmail(@RequestParam String email) {
        log.debug("이메일 중복 확인 - 이메일: {}", email);

        boolean available = memberService.validateEmailDuplicate(email);
        return ResponseEntity.ok(available);
    }

    /**
     * 회원 대여 제한 정보 조회
     */
    @GetMapping("/{id}/loan-limit")
    public ResponseEntity<MemberLoanLimitInfo> getMemberLoanLimitInfo(@PathVariable Long id) {
        log.debug("회원 대여 제한 정보 조회 - 회원ID: {}", id);

        MemberLoanLimitInfo info = memberService.getMemberLoanLimitInfo(id);
        return ResponseEntity.ok(info);
    }
}