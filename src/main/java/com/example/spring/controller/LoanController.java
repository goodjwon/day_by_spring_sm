package com.example.spring.controller;

import com.example.spring.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

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
    // @GetMapping("/api/admin/loans")


    /**
     * [API 명세 #2] 대출 상세 조회
     * GET /api/admin/loans/{id}
     */
    // @GetMapping("/api/admin/loans/{id}")

    /**
     * [API 명세 #3] 신규 대출 생성
     * POST /api/admin/loans
     */
    // @PostMapping("/api/admin/loans")

    /**
     * [API 명세 #4] 대출 정보 수정 (반납, 날짜 연장)
     * PATCH /api/admin/loans/{id}
     */
    // @PatchMapping("/api/admin/loans/{id}")

    /**
     * [API 명세 #5] 대출 기록 삭제
     * DELETE /api/admin/loans/{id}
     */
//     @DeleteMapping("/api/admin/loans/{id}")

    /**
     * [API 명세 #6] 회원 대출 목록 조회 (페이징, 검색, 필터링, 정렬)
     * GET /api/client/loans?page=0&size=10&searchQuery=...&statusFilter=ACTIVE&sortKey=loanDate&sortOrder=desc
    */
    // @GetMapping("/api/client/loans")

    /**
     * [API 명세 #7] 신규 대출 생성
     * POST /api/client/loans
     */
    // @PostMapping("/api/client/loans")

    /**
     * [API 명세 #8] 대출 정보 수정 (반납, 날짜 연장)
     * PATCH /api/client/loans/{id}
     */
    // @PatchMapping("/api/client/loans/{id}")

    /**
     * [API 명세 #9] 대출 기록 삭제
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
