package com.example.spring.exception;

import com.example.spring.domain.model.LoanStatus;

/**
 * 대여 관련 예외 클래스들
 */
public class LoanException {

    /**
     * 대여를 찾을 수 없는 예외
     */
    public static class LoanNotFoundException extends BusinessException {
        public LoanNotFoundException(Long id) {
            super("LOAN_NOT_FOUND", ErrorMessages.loanNotFound(id));
        }
    }

    /**
     * 도서가 이미 대여 중인 예외
     */
    public static class BookAlreadyLoanedException extends BusinessException {
        public BookAlreadyLoanedException(Long bookId) {
            super("BOOK_ALREADY_LOANED", ErrorMessages.bookAlreadyLoaned(bookId));
        }
    }

    /**
     * 도서가 대여 불가능한 예외
     */
    public static class BookNotAvailableException extends BusinessException {
        public BookNotAvailableException(Long bookId) {
            super("BOOK_NOT_AVAILABLE", ErrorMessages.bookNotLoanAvailable(bookId));
        }
    }

    /**
     * 회원의 대여 한도 초과 예외
     */
    public static class LoanLimitExceededException extends BusinessException {
        public LoanLimitExceededException(Long memberId, int currentLoans, int maxLoans) {
            super("LOAN_LIMIT_EXCEEDED", ErrorMessages.loanLimitExceeded(memberId, currentLoans, maxLoans));
        }
    }

    /**
     * 연체 중인 대여가 있는 경우 예외
     */
    public static class OverdueLoansExistException extends BusinessException {
        public OverdueLoansExistException(Long memberId) {
            super("MEMBER_HAS_OVERDUE", ErrorMessages.overdueLoansExist(memberId));
        }
    }

    /**
     * 이미 반납된 대여 예외
     */
    public static class AlreadyReturnedException extends BusinessException {
        public AlreadyReturnedException(Long loanId) {
            super("ALREADY_RETURNED", ErrorMessages.alreadyReturned(loanId));
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
     * 연장 가능 횟수 초과 예외
     */
    public static class ExtensionLimitExceededException extends BusinessException {
        public ExtensionLimitExceededException(int currentExtensions, int maxExtensions) {
            super("EXTENSION_LIMIT_EXCEEDED", ErrorMessages.extensionLimitExceeded(currentExtensions, maxExtensions));
        }
    }

    /**
     * 연장 시기가 너무 이른 예외
     */
    public static class ExtensionTooEarlyException extends BusinessException {
        public ExtensionTooEarlyException(long daysUntilDue) {
            super("EXTENSION_TOO_EARLY", ErrorMessages.extensionTooEarly(daysUntilDue));
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
     * 반납되지 않은 대출을 삭제하려는 예외
     */
    public static class CannotDeleteActiveLoanException extends BusinessException {
        public CannotDeleteActiveLoanException(Long loanId, LoanStatus currentStatus) {
            super("CANNOT_DELETE_ACTIVE_LOAN", ErrorMessages.cannotDeleteActiveLoan(loanId, currentStatus));
        }
    }

    /**
     * 대여에 대한 권한이 없는 예외
     */
    public static class UnauthorizedAccessException extends BusinessException {
        public UnauthorizedAccessException(String message) {
            super("FORBIDDEN", message);
        }
    }
}