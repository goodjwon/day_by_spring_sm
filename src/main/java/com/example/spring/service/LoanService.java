package com.example.spring.service;

import com.example.spring.dto.request.CreateLoanRequest;
import com.example.spring.dto.request.ExtendLoanRequest;
import com.example.spring.dto.response.LoanResponse;
import com.example.spring.entity.Loan;
import com.example.spring.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public interface LoanService {


    //기본 crud.
    /**
     * 대여 정보 등록
     */
    LoanResponse createLoan(CreateLoanRequest request);

    /**
     * 대여 정보 수정
     */
    LoanResponse updateLoan(Long id, ExtendLoanRequest request);

    /**
     * 대여 기록 삭제(Soft Delete)
     */
    void deleteLoan(Long id);

    /**
     * 삭제한 대여 기록 복원
     */
    void deletedLoanRestore(Long id);

    /**
     * 상세보기
     */
    LoanResponse getLoanById(Loan id);

    /**
     * 전체 대여 조회
     */


    //admin
    /**
     * 복합 조건으로 대여 검색
     * @param bookId
     * @param memberId
     * @param status
     * @param dueDate
     * @param pageable
     * @return 검색된 대여 정보
     */
    Page<LoanResponse> searchLoanWithFilters(Long bookId, Long memberId,
                                             LoanStatus status, LocalDateTime dueDate,
                                             Pageable pageable);


    /**
     * 특정 이메일 회원의 연체 대출 목록 조회
     * @param email
     * @param pageable
     * @return
     */
    Page<LoanResponse> getOverDueByEmail(String email, Pageable pageable);

    /**
     * 특정 회원의 대출 이력 조회
     * @param memberId
     * @param pageable
     * @return
     */
    Page<LoanResponse> getLoanByMember(Long memberId, Pageable pageable);

    /**
     * 특정 도서의 대출 이력 조회
     * @param bookId
     * @param pageable
     * @return
     */
    Page<LoanResponse> getBookLoanHistory(Long bookId, Pageable pageable);

    /**
     *모든 대여 목록 조회
     */

    /**
     * 관리자 반납 처리 (반납일 수동 지정 가능)
     */


    /**
     * 관리자 이메일 전송 기능
     */



    //client
    /**
     * 제목으로 대여 조회
     */
    LoanResponse getLoanByTitle(String title);
    /**
     * 회원명으로 대여 조회
     */



//인증된 사용자의 대출 목록 조회 (내 대출)
//사용자 반납 요청 (반납일 서버 자동 설정)
//대출 가능 여부 확인 (재고, 한도, 연체 여부 등 상세 정보 반환)
//대출 연장 (선택 사항)



    /**
     * 대여 날짜 범위로 대여 검색
     */
    List<Loan> searchByDueDateRange(LocalDateTime startDate, LocalDateTime endDate);


    //검증..
    // 도서 재고 및 현재 대출 상태 확인





}
