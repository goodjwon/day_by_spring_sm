package com.example.spring.controller;

import com.example.spring.dto.request.CreateMemberRequest;
import com.example.spring.dto.request.UpdateMemberRequest;
import com.example.spring.dto.response.MemberResponse;
import com.example.spring.entity.MembershipType;
import com.example.spring.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> createMember(
            @Valid @RequestBody CreateMemberRequest request){
        log.info("회원 가입 요청 - 이메일: {}, 이름: {}", request.getEmail(), request.getName());
        MemberResponse response = memberService.createMember(request);
        log.info("회원 가입 성공 - ID: {}, 이메일: {}", response.getId(), response.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> findMemberById(@PathVariable Long id) {
        log.info("회원 조회 요청 - ID: {}", id);
        MemberResponse response = memberService.findMemberById(id);
        log.info("회원 조회 성공 - 이름: {}, 이메일: {}", response.getName(), response.getEmail());

        return ResponseEntity.ok().body(response);

    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMemberRequest request){

        log.info("회원 정보 수정 요청 - ID: {}", id);

        MemberResponse response = memberService.updateMember(id, request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<MemberResponse>> findAllMembers() {

        List<MemberResponse> list = memberService.findAllMembers();
        return  ResponseEntity.ok().body(list);
    }

    /**
     * 이름으로 회원 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<MemberResponse>> findAllMembersByName(@RequestParam String name) {
        List<MemberResponse> list = memberService.findMembersByName(name);
        return  ResponseEntity.ok().body(list);
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
}