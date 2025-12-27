package com.example.spring.repository;


import com.example.spring.entity.Loan;
import com.example.spring.entity.LoanStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


/**
 * Loan 엔티티에 대한 동적 쿼리 Specification
 * 검색, 필터링 조건을 동적으로 생성합니다.
 */
public class LoanSpecification {

    /**
     * 검색어와 상태 필터를 조합한 Specification 생성
     *
     * @param searchQuery 검색어 (도서명, 회원명, 이메일)
     * @param statusFilter 상태 필터 (ACTIVE, OVERDUE, RETURNED, ALL)
     * @return Specification
     */
    public static Specification<Loan> withFilters(String searchQuery, String statusFilter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 검색어 조건 (도서명, 회원명, 이메일로 검색)
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                String likePattern = "%" + searchQuery.trim().toLowerCase() + "%";

                Predicate bookTitlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("book").get("title")),
                        likePattern
                );

                Predicate memberNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("member").get("name")),
                        likePattern
                );

                Predicate memberEmailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("member").get("email")),
                        likePattern
                );

                predicates.add(criteriaBuilder.or(
                        bookTitlePredicate,
                        memberNamePredicate,
                        memberEmailPredicate
                ));
            }

            // 상태 필터 조건
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
                try {
                    LoanStatus status = LoanStatus.valueOf(statusFilter.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // 잘못된 상태 필터는 무시
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 회원 ID로 필터링
     *
     * @param memberId 회원 ID
     * @return Specification
     */
    public static Specification<Loan> byMemberId(Long memberId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("member").get("id"), memberId);
    }

    /**
     * 상태로 필터링
     *
     * @param status 대출 상태
     * @return Specification
     */
    public static Specification<Loan> byStatus(LoanStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    /**
     * 반납되지 않은 대출만 필터링 (returnDate가 null)
     *
     * @return Specification
     */
    public static Specification<Loan> notReturned() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNull(root.get("returnDate"));
    }

    /**
     * 회원 ID와 상태를 조합한 필터
     *
     * @param memberId 회원 ID
     * @param statusFilter 상태 필터 (ACTIVE, OVERDUE, RETURNED, ALL)
     * @return Specification
     */
    public static Specification<Loan> byMemberAndStatus(Long memberId, String statusFilter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 회원 ID 조건
            predicates.add(criteriaBuilder.equal(root.get("member").get("id"), memberId));

            // 상태 필터 조건
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
                try {
                    LoanStatus status = LoanStatus.valueOf(statusFilter.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // 잘못된 상태 필터는 무시
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}