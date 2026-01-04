package com.example.spring.controller;

import com.example.spring.dto.request.CreateLoanRequest;
import com.example.spring.dto.response.LoanResponse;
import com.example.spring.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 대여 관리 REST API 컨트롤러
 * LOAN_GUIDE.md 명세에 따라 구현됨
 */
@RestController
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    /**
     * [API 명세 #1] 전체 대출 목록 조회 (페이징, 검색, 필터링, 정렬)
     * GET /api/admin/loans?page=0&size=10&searchQuery=...&statusFilter=ACTIVE&sortKey=loanDate&sortOrder=desc
     */
    @GetMapping("/api/admin/loans")
    public ResponseEntity<Page<LoanResponse>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "ALL") String statusFilter,
            @RequestParam(defaultValue = "loanDate") String sortKey,
            @RequestParam(defaultValue = "desc") String sortOrder
    ){

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortKey).ascending()
                : Sort.by(sortKey).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LoanResponse> responses = loanService.getAllLoansWithPagination(pageable, searchQuery, statusFilter);

        return ResponseEntity.ok(responses);
    }


    /**
     * [API 명세 #2] 대출 상세 조회
     * GET /api/admin/loans/{id}
     */
    @GetMapping("/api/admin/loans/{id}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long id) {
        LoanResponse response = loanService.getLoanById(id)
                .orElseThrow(()->new RuntimeException("대여를 찾을 수 없습니다."));

        return ResponseEntity.ok(response);
    }

    /**
     * [API 명세 #3] 신규 대출 생성
     * POST /api/admin/loans
     * todo 9 일까지 구현하기, 테스트
     */
    @PostMapping("/api/admin/loans")
    public ResponseEntity<LoanResponse> createLoan(
            @Valid @RequestBody CreateLoanRequest request) {

        LoanResponse response = loanService.createLoan(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * [API 명세 #4] 대출 정보 수정 (반납, 날짜 연장)
     * PATCH /api/admin/loans/{id}
     * todo 9 일까지 구현하기, 테스트
     */
    // @PatchMapping("/api/admin/loans/{id}")

    /**
     * [API 명세 #5] 대출 기록 삭제
     * todo 9 일까지 구현하기, 테스트
     * DELETE /api/admin/loans/{id}
     */
//     @DeleteMapping("/api/admin/loans/{id}")

    /**
     * [API 명세 #6] 회원 대출 목록 조회 (페이징, 검색, 필터링, 정렬)
     * todo 9 일까지 구현하기, 테스트
     * GET /api/client/loans?page=0&size=10&searchQuery=...&statusFilter=ACTIVE&sortKey=loanDate&sortOrder=desc
    */
    // @GetMapping("/api/client/loans")

    /**
     * [API 명세 #7] 신규 대출 생성
     * todo 9 일까지 구현하기, 테스트
     * POST /api/client/loans
     */
    // @PostMapping("/api/client/loans")

    /**
     * [API 명세 #8] 대출 정보 수정 (반납, 날짜 연장)
     * todo 9 일까지 구현하기, 테스트
     * PATCH /api/client/loans/{id}
     */
    // @PatchMapping("/api/client/loans/{id}")

    /**
     * [API 명세 #9] 대출 기록 삭제
     * todo 9 일까지 구현하기, 테스트
     * DELETE /api/client/loans/{id}
     */
    // @DeleteMapping("/api/client/loans/{id}")


    /**
     * [추가 기능] admin 연체된 대출 목록 조회
     */

    /**
     * [추가 기능] admin 현재 대여 중인 목록 조회
     */

    /**
     * [추가 기능] admin 회원 이름으로 대여 조회 (JOIN 활용)
     */

    /**
     * [추가 기능] admin 도서 제목으로 대여 조회 (JOIN 활용)
     */

    /**
     * [추가 기능] admin 모든 대여 조회 (N+1 최적화)
     */

    /**
     * [추가 기능] admin 특정 도서를 대여한 회원 목록 조회
     */

    /**
     * [추가 기능] admin 특정 회원의 현재 대여 중인 도서 목록
     */
    
}
