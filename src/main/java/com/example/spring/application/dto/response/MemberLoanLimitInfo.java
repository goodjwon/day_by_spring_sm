package com.example.spring.application.dto.response;

import com.example.spring.domain.model.MembershipType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원 대여 제한 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberLoanLimitInfo {

    private Long memberId;
    private String memberName;
    private MembershipType membershipType;
    private int maxLoanCount;        // 최대 대여 가능 권수
    private int currentLoanCount;    // 현재 대여 중인 권수
    private int remainingLoanCount;  // 남은 대여 가능 권수
    private boolean canLoan;         // 대여 가능 여부

    /**
     * 대여 가능 여부 계산
     */
    public static MemberLoanLimitInfo of(Long memberId, String memberName,
                                         MembershipType membershipType,
                                         int currentLoanCount) {
        int maxLoanCount = getMaxLoanCountByMembershipType(membershipType);
        int remainingLoanCount = Math.max(0, maxLoanCount - currentLoanCount);
        boolean canLoan = membershipType != MembershipType.SUSPENDED && remainingLoanCount > 0;

        return MemberLoanLimitInfo.builder()
                .memberId(memberId)
                .memberName(memberName)
                .membershipType(membershipType)
                .maxLoanCount(maxLoanCount)
                .currentLoanCount(currentLoanCount)
                .remainingLoanCount(remainingLoanCount)
                .canLoan(canLoan)
                .build();
    }

    /**
     * 멤버십 타입별 최대 대여 권수 반환
     */
    private static int getMaxLoanCountByMembershipType(MembershipType membershipType) {
        return switch (membershipType) {
            case REGULAR -> 5;
            case PREMIUM -> 10;
            case SUSPENDED -> 0;
        };
    }
}