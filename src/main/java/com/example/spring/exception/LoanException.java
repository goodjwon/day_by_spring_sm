package com.example.spring.exception;

public class LoanException {

    /**
     * 대여를 찾을 수 없는 예외
     */
    public static class LoanNotFoundException extends BusinessException {
        public LoanNotFoundException(Long id) {
            super("LOAN_NOT_FOUND", "대여를 찾을 수 없습니다. ID: " + id);
        }
    }

    /**
     * 도서가 이미 대여 중인 예외
     */
    public static class BookAlreadyLoanedException extends BusinessException {
        public BookAlreadyLoanedException(Long bookId) {
            super("BOOK_ALREADY_LOANED", "도서가 이미 대여 중입니다. 도서 ID: " + bookId);
        }
    }

    /**
     * 도서가 대여 불가능한 예외
     */
    public static class BookNotAvailableException extends BusinessException {
        public BookNotAvailableException(Long bookId) {
            super("BOOK_NOT_AVAILABLE", "도서가 대여 불가능합니다. 도서 ID: " + bookId);
        }
    }

    /**
     * 회원의 대여 한도 초과 예외
     */
    public static class LoanLimitExceededException extends BusinessException {
        public LoanLimitExceededException(Long memberId, int currentLoans, int maxLoans) {
            super("LOAN_LIMIT_EXCEEDED",
                    String.format("대여 한도를 초과했습니다. 회원 ID: %d, 현재 대여: %d, 최대 대여: %d",
                            memberId, currentLoans, maxLoans));
        }
    }

    /**
     * 연체 중인 대여가 있는 경우 예외
     */
    public static class OverdueLoansExistException extends BusinessException {
        public OverdueLoansExistException(Long memberId) {
            super("OVERDUE_LOANS_EXIST",
                    "연체 중인 대여가 있어 새로운 대여를 할 수 없습니다. 회원 ID: " + memberId);
        }
    }

    /**
     * 이미 반납된 대여 예외
     */
    public static class AlreadyReturnedException extends BusinessException {
        public AlreadyReturnedException(Long loanId) {
            super("ALREADY_RETURNED", "이미 반납된 대여입니다. 대여 ID: " + loanId);
        }
    }

    /**
     * 대여 연장 불가능 예외
     */
    public static class LoanExtensionNotAllowedException extends BusinessException {
        public LoanExtensionNotAllowedException(String message) {
            super("LOAN_EXTENSION_NOT_ALLOWED", message);
        }
    }

    /**
     * 잘못된 대여 상태 예외
     */
    public static class InvalidLoanStateException extends BusinessException {
        public InvalidLoanStateException(String message) {
            super("INVALID_LOAN_STATE", message);
        }
    }

    /**
     * 대여 기간 유효성 예외
     */
    public static class InvalidLoanPeriodException extends BusinessException {
        public InvalidLoanPeriodException(String message) {
            super("INVALID_LOAN_PERIOD", message);
        }
    }

    /**
     * 대여에 대한 권한이 없는 예외
     */
    public static class UnauthorizedAccessException extends BusinessException {
        public UnauthorizedAccessException(String message) {
            super("UNAUTHORIZED_ACCESS", message);
        }
    }

    public static class ExtensionTooEarlyException extends BusinessException {
        public ExtensionTooEarlyException(String message) {
            super("EXTENSION_TOO_EARLY", message);
        }
    }

    public static class ExtensionLimitExceededException extends BusinessException {
        public ExtensionLimitExceededException(String message) {
            super("EXTENSION_LIMIT_EXCEEDED", message);
        }
    }
}
