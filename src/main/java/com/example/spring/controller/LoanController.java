package com.example.spring.controller;

import com.example.spring.dto.request.CreateLoanRequest;
import com.example.spring.dto.request.UpdateLoanRequest;
import com.example.spring.dto.response.LoanResponse;
import com.example.spring.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 대여 관리 REST API 컨트롤러
 * LOAN_GUIDE.md 명세에 따라 구현됨
 */
@RestController
@RequiredArgsConstructor
@Slf4j
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
    @PatchMapping("/api/admin/loans/{id}")
    public ResponseEntity<LoanResponse> updateLoan(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLoanRequest request) {

        log.info("대출 정보 수정 요청 - ID: {}, status: {}, dueDate: {}",
                id, request.getStatus(), request.getDueDate());

        LoanResponse response = loanService.updateLoan(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/admin/loans/member/{memberId}/borrowed-books")
    public ResponseEntity<List<com.example.spring.dto.response.BookResponse>> getCurrentlyBorrowedBooksByMember(
            @PathVariable Long memberId) {

        log.debug("특정 회원의 현재 대여 중인 도서 목록 - 회원 ID: {}", memberId);

        List<com.example.spring.dto.response.BookResponse> response =
                loanService.getCurrentlyBorrowedBooksByMember(memberId);
        return ResponseEntity.ok(response);
    }


    /**
     * [API 명세 #5] 대출 기록 삭제
     * todo 9 일까지 구현하기, 테스트
     * DELETE /api/admin/loans/{id}
     */
    //@DeleteMapping("/api/admin/loans/{id}")
    @DeleteMapping("/api/admin/loans/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        log.info("대출 기록 삭제 요청 - ID: {}", id);

        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * [API 명세 #6] 회원 대출 목록 조회 (페이징, 검색, 필터링, 정렬)
     * todo 9 일까지 구현하기, 테스트
     * GET /api/client/loans?page=0&size=10&searchQuery=...&statusFilter=ACTIVE&sortKey=loanDate&sortOrder=desc
    */
    // @GetMapping("/api/client/loans")
    @GetMapping("/api/client/loans")
    public ResponseEntity<List<LoanResponse>> getMyLoans(
            @RequestParam Long memberId, // TODO: 추후 인증 구현 시 토큰에서 추출
            @RequestParam(defaultValue = "ALL") String statusFilter
    ) {
        log.debug("내 대출 목록 조회 - 회원 ID: {}, 상태 필터: {}", memberId, statusFilter);

        List<LoanResponse> response = loanService.getMyLoans(memberId, statusFilter);
        return ResponseEntity.ok(response);
    }


    /**
     * [추가 기능] admin 연체된 대출 목록 조회
     */
    @GetMapping("/api/admin/loans/overdue")
    public ResponseEntity<List<LoanResponse>> getOverdueLoans() {
        log.debug("연체된 대여 목록 조회");

        List<LoanResponse> response = loanService.getOverdueLoans();
        return ResponseEntity.ok(response);
    }


    /**
     * [추가 기능] admin 현재 대여 중인 목록 조회
     */
    @GetMapping("/api/admin/loans/active")
    public ResponseEntity<List<LoanResponse>> getActiveLoans() {
        log.debug("현재 대여 중인 목록 조회");

        List<LoanResponse> response = loanService.getActiveLoans();
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] admin 회원 이름으로 대여 조회 (JOIN 활용)
     */
    @GetMapping("/api/admin/loans/member/{memberId}")
    public ResponseEntity<List<LoanResponse>> getLoansByMember(@PathVariable Long memberId) {
        log.debug("회원별 대여 내역 조회 - 회원 ID: {}", memberId);

        List<LoanResponse> response = loanService.getLoansByMemberId(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] admin 도서 제목으로 대여 조회 (JOIN 활용)
     */
    @GetMapping("/api/admin/loans/book/{bookId}")
    public ResponseEntity<List<LoanResponse>> getLoansByBook(@PathVariable Long bookId) {
        log.debug("도서별 대여 내역 조회 - 도서 ID: {}", bookId);

        List<LoanResponse> response = loanService.getLoansByBookId(bookId);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] admin 모든 대여 조회 (N+1 최적화)
     */
    @GetMapping("/api/admin/loans/search/by-member-name")
    public ResponseEntity<List<LoanResponse>> getLoansByMemberName(
            @RequestParam String name) {
        log.debug("회원 이름으로 대여 조회 - 이름: {}", name);

        List<LoanResponse> response = loanService.getLoansByMemberName(name);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] admin 특정 도서를 대여한 회원 목록 조회
     */
    @GetMapping("/api/admin/loans/search/by-book-title")
    public ResponseEntity<List<LoanResponse>> getLoansByBookTitle(@RequestParam String title) {
        log.debug("도서 제목으로 대여 조회 - 제목: {}", title);

        List<LoanResponse> response = loanService.getLoansByBookTitle(title);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] admin 특정 회원의 현재 대여 중인 도서 목록
     */
    @GetMapping("/api/admin/loans/with-details")
    public ResponseEntity<List<LoanResponse>> getAllLoansWithDetails() {
        log.debug("모든 대여 조회 (회원, 도서 정보 포함)");

        List<LoanResponse> response = loanService.getAllLoansWithDetails();
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] 연체 중인 대여 조회 (회원 정보 포함)
     */
    @GetMapping("/api/admin/loans/overdue/with-member-info")
    public ResponseEntity<List<LoanResponse>> getOverdueLoansWithMemberInfo() {
        log.debug("연체 중인 대여 조회 (회원 정보 포함)");

        List<LoanResponse> response = loanService.getOverdueLoansWithMemberInfo();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/client/loans/{id}/return")
    public ResponseEntity<LoanResponse> returnBookByMember(
            @PathVariable Long id,
            @RequestParam Long memberId // TODO: 추후 인증 구현 시 토큰에서 추출
    ) {
        log.info("도서 반납 신청 (사용자) - 대출 ID: {}, 회원 ID: {}", id, memberId);

        LoanResponse response = loanService.returnBookByMember(id, memberId);
        return ResponseEntity.ok(response);
    }

}
