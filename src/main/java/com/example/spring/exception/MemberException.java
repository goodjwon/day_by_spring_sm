package com.example.spring.exception;

public class MemberException {
    public static class MemberNotFoundException extends BusinessException {

        public MemberNotFoundException(Long memberId) {
            super("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다. ID: " + memberId);
        }

        public MemberNotFoundException(String email) {
            super("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다. 이메일: " + email);
        }

        public MemberNotFoundException(String errorCode, String message) {
            super(errorCode, message);
        }

        public MemberNotFoundException(String errorCode, String message, Throwable cause) {
            super(errorCode, message, cause);
        }
    }
}
