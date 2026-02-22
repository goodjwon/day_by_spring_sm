package com.example.spring.exception;

/**
 * 회원 관련 예외 클래스들
 */
public class MemberException {

    /**
     * 회원을 찾을 수 없는 예외
     */
    public static class MemberNotFoundException extends BusinessException {
        private final Long memberId;

        public MemberNotFoundException(Long id) {
            super("MEMBER_NOT_FOUND", ErrorMessages.memberNotFound(id));
            this.memberId = id;
        }

        public MemberNotFoundException(String entityName, Long id) {
            super("MEMBER_NOT_FOUND", entityName + "을(를) 찾을 수 없습니다. ID: " + id);
            this.memberId = id;
        }

        public MemberNotFoundException(String message) {
            super("MEMBER_NOT_FOUND", message);
            this.memberId = null;
        }

        public Long getMemberId() {
            return memberId;
        }
    }

    /**
     * 이메일 중복 예외
     */
    public static class DuplicateEmailException extends BusinessException {
        public DuplicateEmailException(String email) {
            super("DUPLICATE_EMAIL", ErrorMessages.DUPLICATE_EMAIL_EXISTING_PREFIX + email);
        }
    }

    /**
     * 잘못된 회원 데이터 예외
     */
    public static class InvalidMemberDataException extends BusinessException {
        public InvalidMemberDataException(String message) {
            super("INVALID_MEMBER_DATA", message);
        }
    }

    /**
     * 삭제된 회원 접근 예외
     */
    public static class DeletedMemberAccessException extends BusinessException {
        public DeletedMemberAccessException(String message) {
            super("DELETED_MEMBER_ACCESS", message);
        }
    }
}