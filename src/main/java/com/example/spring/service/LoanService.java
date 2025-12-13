package com.example.spring.service;

import com.example.spring.dto.request.CreateLoanRequest;
import com.example.spring.dto.request.ExtendLoanRequest;
import com.example.spring.dto.request.UpdateLoanRequest;
import com.example.spring.dto.response.BookResponse;
import com.example.spring.dto.response.LoanResponse;
import com.example.spring.dto.response.MemberResponse;
import com.example.spring.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * LoanService 인터페이스
 * 대여 관리 비즈니스 로직을 정의합니다.
 *
 * ========================================
 * API 명세서 기반 메서드
 * ========================================
 */
public interface LoanService {

    // ========== 관리자(Admin) API - 기본 CRUD ==========

    /**
     * [API 명세 #3] 신규 대출 생성
     * POST /api/admin/loans
     *
     * @param request 대여 생성 요청 (memberId, bookId, loanDate, dueDate)
     * @return 생성된 대여 응답
     * @throws com.example.spring.exception.LoanException.BookAlreadyLoanedException 도서가 이미 대여 중인 경우
     * @throws com.example.spring.exception.LoanException.BookNotAvailableException 도서가 대여 불가능한 경우
     * @throws com.example.spring.exception.LoanException.LoanLimitExceededException 대여 한도 초과
     * @throws com.example.spring.exception.LoanException.OverdueLoansExistException 연체 중인 대여가 있는 경우
     */
    LoanResponse createLoan(CreateLoanRequest request);

    /**
     * [API 명세 #1] 전체 대출 목록 조회 (페이징, 검색, 필터링, 정렬)
     * GET /api/admin/loans?page=0&size=10&searchQuery=...&statusFilter=ACTIVE&sortKey=loanDate&sortOrder=desc
     *
     * @param pageable 페이징 및 정렬 정보
     * @param searchQuery 검색어 (도서명, 회원명, 이메일)
     * @param statusFilter 대출 상태 필터 (ACTIVE, OVERDUE, RETURNED, ALL)
     * @return 페이징된 대여 목록
     */
    Page<LoanResponse> getAllLoansWithPagination(Pageable pageable, String searchQuery, String statusFilter);

    /**
     * [API 명세 #2] 단일 대출 상세 조회
     * GET /api/admin/loans/{id}
     *
     * @param id 대여 ID
     * @return 대여 상세 정보
     */
    Optional<LoanResponse> getLoanById(Long id);

    /**
     * [API 명세 #4] 대출 정보 수정 (반납, 날짜 연장)
     * PATCH /api/admin/loans/{id}
     *
     * @param loanId 대여 ID
     * @param request 수정 요청 (status 또는 dueDate)
     * @return 수정된 대여 응답
     * @throws com.example.spring.exception.LoanException.LoanNotFoundException 대여를 찾을 수 없는 경우
     */
    LoanResponse updateLoan(Long loanId, UpdateLoanRequest request);

    /**
     * [API 명세 #5] 대출 기록 삭제
     * DELETE /api/admin/loans/{id}
     *
     * @param loanId 대여 ID
     * @throws com.example.spring.exception.LoanException.LoanNotFoundException 대여를 찾을 수 없는 경우
     */
    void deleteLoan(Long loanId);

    // ========== 사용자(Client) API ==========

    /**
     * [API 명세 Client #1] 내 대출 목록 조회
     * GET /api/my/loans?statusFilter=ACTIVE
     *
     * @param memberId 회원 ID (인증된 사용자)
     * @param statusFilter 대출 상태 필터 (ACTIVE, OVERDUE, RETURNED, ALL)
     * @return 내 대여 목록
     */
    List<LoanResponse> getMyLoans(Long memberId, String statusFilter);

    /**
     * [API 명세 Client #2] 도서 반납 신청
     * POST /api/my/loans/{id}/return
     *
     * @param loanId 대여 ID
     * @param memberId 회원 ID (인증된 사용자, 소유권 확인용)
     * @return 반납 처리된 대여 응답
     * @throws com.example.spring.exception.LoanException.LoanNotFoundException 대여를 찾을 수 없는 경우
     * @throws com.example.spring.exception.LoanException.AlreadyReturnedException 이미 반납된 경우
     * @throws com.example.spring.exception.LoanException.UnauthorizedAccessException 대여 소유자가 아닌 경우
     */
    LoanResponse returnBookByMember(Long loanId, Long memberId);

    // ========================================
    // 추가 기능 메서드 (API 명세 범위 외)
    // ========================================

    /**
     * [추가 기능] 모든 대여 조회 (페이징 없음)
     * @return 대여 목록
     */
    List<LoanResponse> getAllLoans();

    /**
     * [추가 기능] 회원별 대여 내역 조회
     * @param memberId 회원 ID
     * @return 대여 목록
     */
    List<LoanResponse> getLoansByMemberId(Long memberId);

    /**
     * [추가 기능] 도서별 대여 내역 조회
     * @param bookId 도서 ID
     * @return 대여 목록
     */
    List<LoanResponse> getLoansByBookId(Long bookId);

    /**
     * [추가 기능] 현재 대여 중인 목록 조회 (반납되지 않은 대여)
     * @return 대여 중인 목록
     */
    List<LoanResponse> getActiveLoans();

    /**
     * [추가 기능] 회원의 현재 대여 중인 목록 조회
     * @param memberId 회원 ID
     * @return 대여 중인 목록
     */
    List<LoanResponse> getActiveLoansByMemberId(Long memberId);

    /**
     * [추가 기능] 연체된 대여 목록 조회
     * @return 연체 목록
     */
    List<LoanResponse> getOverdueLoans();

    /**
     * [추가 기능] 기간별 대여 내역 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 대여 목록
     */
    List<LoanResponse> getLoansByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * [추가 기능] 도서 반납 처리 (관리자용, 소유권 확인 없음)
     * @param loanId 대여 ID
     * @return 반납 처리된 대여 응답
     * @throws com.example.spring.exception.LoanException.LoanNotFoundException 대여를 찾을 수 없는 경우
     * @throws com.example.spring.exception.LoanException.AlreadyReturnedException 이미 반납된 경우
     */
    LoanResponse returnBook(Long loanId);

    /**
     * [추가 기능] 대여 연장
     * @param loanId 대여 ID
     * @param request 연장 요청 (일수)
     * @return 연장된 대여 응답
     * @throws com.example.spring.exception.LoanException.LoanNotFoundException 대여를 찾을 수 없는 경우
     * @throws com.example.spring.exception.LoanException.LoanExtensionNotAllowedException 연장 불가능한 경우
     */
    LoanResponse extendLoan(Long loanId, ExtendLoanRequest request);

    /**
     * [추가 기능] 대여 취소
     * @param loanId 대여 ID
     * @throws com.example.spring.exception.LoanException.LoanNotFoundException 대여를 찾을 수 없는 경우
     * @throws com.example.spring.exception.LoanException.InvalidLoanStateException 취소 불가능한 상태
     */
    void cancelLoan(Long loanId);

    /**
     * [추가 기능] 연체료 조회
     * @param loanId 대여 ID
     * @return 연체료 (원)
     */
    java.math.BigDecimal getOverdueFee(Long loanId);

    /**
     * [추가 기능] 연체 대여 수 조회
     * @return 연체 대여 수
     */
    long getOverdueLoansCount();

    /**
     * [추가 기능] 회원의 대여 가능 여부 확인
     * @param memberId 회원 ID
     * @return 대여 가능 여부
     */
    boolean canMemberLoan(Long memberId);

    /**
     * [추가 기능] 도서의 대여 가능 여부 확인
     * @param bookId 도서 ID
     * @return 대여 가능 여부
     */
    boolean isBookAvailableForLoan(Long bookId);

    // ========== JOIN 활용 메소드들 (추가 기능) ==========

    /**
     * [추가 기능] 회원 이름으로 대여 조회
     * @param name 회원 이름
     * @return 대여 목록
     */
    List<LoanResponse> getLoansByMemberName(String name);

    /**
     * [추가 기능] 도서 제목으로 대여 조회
     * @param title 도서 제목
     * @return 대여 목록
     */
    List<LoanResponse> getLoansByBookTitle(String title);

    /**
     * [추가 기능] 특정 회원의 연체된 대여 조회 (이메일 기준)
     * @param email 회원 이메일
     * @return 연체 대여 목록
     */
    List<LoanResponse> getOverdueLoansByMemberEmail(String email);

    /**
     * [추가 기능] 모든 대여 조회 (회원, 도서 정보 포함 - N+1 문제 해결)
     * Fetch Join을 사용하여 성능 최적화
     * @return 대여 목록
     */
    List<LoanResponse> getAllLoansWithDetails();

    /**
     * [추가 기능] 특정 도서를 대여한 회원 목록 조회
     * @param bookTitle 도서 제목
     * @return 회원 목록
     */
    List<MemberResponse> getMembersByBookTitle(String bookTitle);

    /**
     * [추가 기능] 특정 회원의 현재 대여 중인 도서 목록
     * @param memberId 회원 ID
     * @return 도서 목록
     */
    List<BookResponse> getCurrentlyBorrowedBooksByMember(Long memberId);

    /**
     * [추가 기능] 연체 중인 대여 조회 (회원 정보 포함)
     * 반납 예정일 오름차순 정렬
     * @return 연체 대여 목록
     */
    List<LoanResponse> getOverdueLoansWithMemberInfo();
}
