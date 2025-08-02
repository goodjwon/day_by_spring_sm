package com.example.spring.exception;

public class DuplicateEmailException extends BusinessException  {

    public DuplicateEmailException(String email) {
        super("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다: " + email);
    }

}
