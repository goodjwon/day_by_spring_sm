package com.example.spring.presentation.controller;

import com.example.spring.application.dto.request.ClientLoanRequest;
import com.example.spring.application.dto.request.CreateLoanRequest;
import com.example.spring.infrastructure.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.spring.application.dto.request.UpdateLoanRequest;
import com.example.spring.application.dto.response.LoanResponse;
import com.example.spring.application.LoanService;
import com.example.spring.exception.LoanException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 대여 관리 REST API 컨트롤러
 * LOAN_GUIDE.md 명세에 따라 구현됨
 */
@Tag(name = "Loan", description = "도서 대여 관리 API")
@Slf4j
@RestController
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    // ========================================
    // 관리자 API (Admin)
    // ========================================

    /**
     * [API 명세 #1] 전체 대출 목록 조회 (페이징, 검색, 필터링, 정렬)
     * GET /api/admin/loans?page=0&size=10&searchQuery=...&statusFilter=ACTIVE&sortKey=loanDate&sortOrder=desc
     */
    @Operation(
            summary = "[API 명세 #1] 전체 대출 목록 조회 (페이징)",
            description = "모든 대출 기록을 검색, 필터링, 정렬, 페이징하여 조회합니다. 관리자(Admin) API에 해당합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/api/admin/loans")
    public ResponseEntity<Page<LoanResponse>> getAllLoans(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 당 항목 수", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "검색어 (도서명, 회원명, 이메일)", example = "홍길동")
            @RequestParam(required = false) String searchQuery,

            @Parameter(description = "대출 상태 필터 (ACTIVE, OVERDUE, RETURNED, ALL)", example = "ACTIVE")
            @RequestParam(defaultValue = "ALL") String statusFilter,

            @Parameter(description = "정렬 기준 (loanDate, dueDate, bookTitle)", example = "loanDate")
            @RequestParam(defaultValue = "loanDate") String sortKey,

            @Parameter(description = "정렬 순서 (asc, desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        log.debug("전체 대출 목록 조회 (페이징) - page: {}, size: {}, searchQuery: {}, statusFilter: {}, sortKey: {}, sortOrder: {}",
                page, size, searchQuery, statusFilter, sortKey, sortOrder);

        // 정렬 설정
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortKey).ascending()
                : Sort.by(sortKey).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LoanResponse> response = loanService.getAllLoansWithPagination(pageable, searchQuery, statusFilter);
        return ResponseEntity.ok(response);
    }

    /**
     * [API 명세 #2] 대출 상세 조회
     * GET /api/admin/loans/{id}
     */
    @Operation(
            summary = "[API 명세 #2] 대출 상세 조회",
            description = "특정 대출의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "대출을 찾을 수 없음")
    })
    @GetMapping("/api/admin/loans/{id}")
    public ResponseEntity<LoanResponse> getLoan(@PathVariable Long id) {
        log.debug("대출 조회 요청 - ID: {}", id);

        LoanResponse response = loanService.getLoanById(id)
                .orElseThrow(() -> new LoanException.LoanNotFoundException(id));
        return ResponseEntity.ok(response);
    }

    /**
     * [API 명세 #3] 신규 대출 생성
     * POST /api/admin/loans
     */
    @Operation(
            summary = "[API 명세 #3] 도서 대출 생성",
            description = "회원이 도서를 대여합니다. 회원의 대여 가능 여부와 도서의 대여 가능 상태를 확인한 후 대여를 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "대여 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락 등)"),
            @ApiResponse(responseCode = "409", description = "대여 불가 (도서가 이미 대여 중이거나 회원이 대여 불가 상태)")
    })
    @PostMapping("/api/admin/loans")
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        log.info("도서 대여 요청 - 회원 ID: {}, 도서 ID: {}", request.getMemberId(), request.getBookId());

        LoanResponse response = loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * [API 명세 #4] 대출 정보 수정 (반납, 날짜 연장)
     * PATCH /api/admin/loans/{id}
     */
    @Operation(
            summary = "[API 명세 #4] 대출 정보 수정",
            description = "대출 상태를 변경하거나 반납일을 연장합니다. status를 RETURNED로 변경하면 반납 처리되고, dueDate를 수정하면 기간이 연장됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "대출을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PatchMapping("/api/admin/loans/{id}")
    public ResponseEntity<LoanResponse> updateLoan(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLoanRequest request) {

        log.info("대출 정보 수정 요청 - ID: {}, status: {}, dueDate: {}",
                id, request.getStatus(), request.getDueDate());

        LoanResponse response = loanService.updateLoan(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * [API 명세 #5] 대출 기록 삭제
     * DELETE /api/admin/loans/{id}
     */
    @Operation(
            summary = "[API 명세 #5] 대출 기록 삭제",
            description = "대출 기록을 삭제합니다. 대출 중인 경우 도서 상태가 자동으로 복원됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "대출을 찾을 수 없음")
    })
    @DeleteMapping("/api/admin/loans/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        log.info("대출 기록 삭제 요청 - ID: {}", id);

        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // 사용자 API (Client)
    // ========================================

    /**
     * [API 명세 Client #1] 내 대출 목록 조회
     * GET /api/client/loans?statusFilter=ACTIVE
     */
    @Operation(
            summary = "[API 명세 Client #1] 내 대출 목록 조회",
            description = "인증된 사용자의 대출 목록을 조회합니다. 상태 필터를 적용할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/api/client/loans")
    public ResponseEntity<List<LoanResponse>> getMyLoans(
            @AuthenticationPrincipal CustomUserDetails user,

            @Parameter(description = "대출 상태 필터 (ACTIVE, OVERDUE, RETURNED, ALL)", example = "ACTIVE")
            @RequestParam(defaultValue = "ALL") String statusFilter
    ) {
        log.debug("내 대출 목록 조회 - 회원 ID: {}, 상태 필터: {}", user.getMemberId(), statusFilter);

        List<LoanResponse> response = loanService.getMyLoans(user.getMemberId(), statusFilter);
        return ResponseEntity.ok(response);
    }

    /**
     * [API 명세 Client #2] 도서 반납 신청
     * POST /api/client/loans/{id}/return
     */
    @Operation(
            summary = "[API 명세 Client #2] 도서 반납 신청",
            description = "사용자가 직접 도서를 반납했음을 시스템에 알립니다. 대출 소유자만 반납할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반납 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 대출)"),
            @ApiResponse(responseCode = "404", description = "대출을 찾을 수 없음")
    })
    @PostMapping("/api/client/loans/{id}/return")
    public ResponseEntity<LoanResponse> returnBookByMember(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("도서 반납 신청 (사용자) - 대출 ID: {}, 회원 ID: {}", id, user.getMemberId());

        LoanResponse response = loanService.returnBookByMember(id, user.getMemberId());
        return ResponseEntity.ok(response);
    }

    /**
     * [API 명세 Client #3] 대출 이력 삭제
     * DELETE /api/client/loans/{id}
     */
    @Operation(
            summary = "[API 명세 Client #3] 대출 이력 삭제",
            description = "반납 완료된 대출 기록을 삭제합니다. 반납되지 않은 대출은 삭제할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 대출)"),
            @ApiResponse(responseCode = "404", description = "대출을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "반납되지 않은 대출 삭제 시도")
    })
    @DeleteMapping("/api/client/loans/{id}")
    public ResponseEntity<Void> deleteLoanByMember(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("대출 이력 삭제 요청 (사용자) - 대출 ID: {}, 회원 ID: {}", id, user.getMemberId());

        loanService.deleteLoanByMember(id, user.getMemberId());
        return ResponseEntity.noContent().build();
    }

    /**
     * [API 명세 Client #4] 사용자 대출 신청
     * POST /api/client/loans/request
     */
    @Operation(
            summary = "[API 명세 Client #4] 사용자 대출 신청",
            description = "인증된 사용자가 도서를 대출 신청합니다. 대출 가능 여부(권수 제한, 연체 여부)를 검증합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "대출 신청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락 등)"),
            @ApiResponse(responseCode = "409", description = "대출 불가 (도서가 이미 대출 중이거나 회원이 대출 불가 상태)")
    })
    @PostMapping("/api/client/loans/request")
    public ResponseEntity<LoanResponse> createLoanByMember(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ClientLoanRequest request
    ) {
        log.info("사용자 대출 신청 - 회원 ID: {}, 도서 ID: {}, 대출 기간: {}일",
                user.getMemberId(), request.getBookId(), request.getLoanPeriod());

        LoanResponse response = loanService.createLoanByMember(user.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========================================
    // 추가 유틸리티 API (선택적)
    // ========================================

    /**
     * [추가 기능] 연체된 대출 목록 조회
     */
    @Operation(summary = "연체된 대출 목록 조회", description = "현재 연체 중인 모든 대출을 조회합니다.")
    @GetMapping("/api/admin/loans/overdue")
    public ResponseEntity<List<LoanResponse>> getOverdueLoans() {
        log.debug("연체된 대여 목록 조회");

        List<LoanResponse> response = loanService.getOverdueLoans();
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] 현재 대여 중인 목록 조회
     */
    @Operation(summary = "현재 대여 중인 목록 조회", description = "반납되지 않은 모든 대출을 조회합니다.")
    @GetMapping("/api/admin/loans/active")
    public ResponseEntity<List<LoanResponse>> getActiveLoans() {
        log.debug("현재 대여 중인 목록 조회");

        List<LoanResponse> response = loanService.getActiveLoans();
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] Admin - 특정 회원의 전체 대여 이력 조회
     *
     * ✅ 반환: List<LoanResponse> (대여 기록 - 날짜, 상태, 연체료 등 포함)
     * ✅ 범위: 과거 + 현재 모든 대여 기록
     *
     * 사용 예시: 관리자가 회원 상세 화면에서 "대여 이력" 탭 클릭 시
     */
    @Operation(summary = "회원별 전체 대여 이력 조회",
            description = "특정 회원의 모든 대여 기록을 조회합니다 (반납 완료 포함)")
    @GetMapping("/api/admin/loans/member/{memberId}")
    public ResponseEntity<List<LoanResponse>> getLoansByMember(@PathVariable Long memberId) {
        log.debug("회원별 대여 내역 조회 - 회원 ID: {}", memberId);

        List<LoanResponse> response = loanService.getLoansByMemberId(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] Admin - 특정 도서의 전체 대여 이력 조회
     *
     * ✅ 반환: List<LoanResponse> (대여 기록 - 누가, 언제 빌렸는지)
     * ✅ 범위: 과거 + 현재 모든 대여 기록
     *
     * 사용 예시: 관리자가 도서 상세 화면에서 "대여 이력" 탭 클릭 시
     */
    @Operation(summary = "도서별 전체 대여 이력 조회",
            description = "특정 도서의 모든 대여 기록을 조회합니다 (이전 대여자 포함)")
    @GetMapping("/api/admin/loans/book/{bookId}")
    public ResponseEntity<List<LoanResponse>> getLoansByBook(@PathVariable Long bookId) {
        log.debug("도서별 대여 내역 조회 - 도서 ID: {}", bookId);

        List<LoanResponse> response = loanService.getLoansByBookId(bookId);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] 회원 이름으로 대여 조회 (JOIN 활용)
     */
    @Operation(summary = "회원 이름으로 대여 조회", description = "JOIN 쿼리를 사용하여 회원 이름으로 대여 내역을 검색합니다.")
    @GetMapping("/api/admin/loans/search/by-member-name")
    public ResponseEntity<List<LoanResponse>> getLoansByMemberName(
            @Parameter(description = "회원 이름", required = true, example = "홍길동")
            @RequestParam String name) {
        log.debug("회원 이름으로 대여 조회 - 이름: {}", name);

        List<LoanResponse> response = loanService.getLoansByMemberName(name);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] 도서 제목으로 대여 조회 (JOIN 활용)
     */
    @Operation(summary = "도서 제목으로 대여 조회", description = "JOIN 쿼리를 사용하여 도서 제목으로 대여 내역을 검색합니다.")
    @GetMapping("/api/admin/loans/search/by-book-title")
    public ResponseEntity<List<LoanResponse>> getLoansByBookTitle(@RequestParam String title) {
        log.debug("도서 제목으로 대여 조회 - 제목: {}", title);

        List<LoanResponse> response = loanService.getLoansByBookTitle(title);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] 모든 대여 조회 (N+1 최적화)
     */
    @Operation(summary = "모든 대여 조회 (N+1 최적화)", description = "Fetch Join을 사용하여 회원과 도서 정보를 함께 조회합니다.")
    @GetMapping("/api/admin/loans/with-details")
    public ResponseEntity<List<LoanResponse>> getAllLoansWithDetails() {
        log.debug("모든 대여 조회 (회원, 도서 정보 포함)");

        List<LoanResponse> response = loanService.getAllLoansWithDetails();
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] Admin - 특정 도서를 빌린 적 있는 회원 목록 조회
     *
     * ⭐ 반환: List<MemberResponse> (회원 정보 - 이름, 이메일, 등급 등)
     * ⭐ 검색: 도서 제목(title)으로 검색
     * ⭐ 용도: "이 책을 누가 빌렸나?" 확인
     *
     * 사용 예시: 관리자가 인기 도서의 독자층 분석 시
     */
    @Operation(summary = "도서를 대여한 회원 목록 조회",
            description = "특정 도서(제목)를 빌린 적 있는 모든 회원 목록을 조회합니다")
    @GetMapping("/api/admin/loans/search/members-by-book-title")
    public ResponseEntity<List<com.example.spring.application.dto.response.MemberResponse>> getMembersByBookTitle(
            @RequestParam String title) {

        log.debug("특정 도서를 대여한 회원 목록 조회 - 도서 제목: {}", title);

        List<com.example.spring.application.dto.response.MemberResponse> response = loanService.getMembersByBookTitle(title);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] Admin - 특정 회원이 현재 빌리고 있는 도서 목록 조회
     *
     * ⭐ 반환: List<BookResponse> (도서 정보 - 제목, 저자, ISBN 등)
     * ⭐ 범위: 현재 대여 중인 도서만 (반납하지 않은 것)
     * ⭐ 용도: "이 회원이 지금 뭘 빌렸나?" 확인
     *
     * 사용 예시: 관리자가 회원의 현재 대여 현황을 빠르게 확인 시
     */
    @Operation(summary = "회원의 현재 대여 중인 도서 목록 조회",
            description = "특정 회원이 현재 빌리고 있는 도서 목록을 조회합니다 (미반납만)")
    @GetMapping("/api/admin/loans/member/{memberId}/borrowed-books")
    public ResponseEntity<List<com.example.spring.application.dto.response.BookResponse>> getCurrentlyBorrowedBooksByMember(
            @PathVariable Long memberId) {

        log.debug("특정 회원의 현재 대여 중인 도서 목록 - 회원 ID: {}", memberId);

        List<com.example.spring.application.dto.response.BookResponse> response =
                loanService.getCurrentlyBorrowedBooksByMember(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가 기능] 연체 중인 대여 조회 (회원 정보 포함)
     */
    @Operation(summary = "연체 중인 대여 조회 (회원 정보 포함)", description = "반납 예정일 오름차순 정렬")
    @GetMapping("/api/admin/loans/overdue/with-member-info")
    public ResponseEntity<List<LoanResponse>> getOverdueLoansWithMemberInfo() {
        log.debug("연체 중인 대여 조회 (회원 정보 포함)");

        List<LoanResponse> response = loanService.getOverdueLoansWithMemberInfo();
        return ResponseEntity.ok(response);
    }
}