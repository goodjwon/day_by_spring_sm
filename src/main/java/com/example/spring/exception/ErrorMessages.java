package com.example.spring.exception;

/**
 * 예외/에러 응답 공통 메시지 카탈로그
 */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static final String VALIDATION_ERROR = "입력값이 올바르지 않습니다";
    public static final String BINDING_FAILED = "요청 파라미터 바인딩에 실패했습니다.";
    public static final String CONSTRAINT_VIOLATION = "요청 값 제약 조건을 위반했습니다.";
    public static final String RESOURCE_NOT_FOUND = "요청한 리소스를 찾을 수 없습니다.";
    public static final String INTERNAL_SERVER_ERROR = "서버 내부 오류가 발생했습니다.";
    public static final String AUTHENTICATION_FAILED = "이메일 또는 비밀번호가 올바르지 않습니다.";

    public static final String DUPLICATE_EMAIL_EXISTING_PREFIX = "이미 존재하는 이메일입니다: ";
    public static final String DUPLICATE_EMAIL_IN_USE_PREFIX = "이미 사용 중인 이메일입니다: ";
    public static final String EMPTY_ORDER_ITEMS = "주문 항목이 비어있습니다.";

    public static final String LOAN_RETURNED_CANNOT_EXTEND = "이미 반납된 대여는 연장할 수 없습니다";
    public static final String LOAN_OVERDUE_CANNOT_EXTEND = "연체된 대여는 연장할 수 없습니다";
    public static final String LOAN_RETURNED_CANNOT_CANCEL = "이미 반납된 대여는 취소할 수 없습니다";
    public static final String LOAN_DUE_DATE_MUST_BE_FUTURE = "반납 예정일은 현재 시간보다 미래여야 합니다";

    public static final String ORDER_ITEM_QUANTITY_MIN_ONE = "수량은 1 이상이어야 합니다";
    public static final String ISBN_REQUIRED = "ISBN은 필수입니다";
    public static final String ADDRESS_REQUIRED = "주소는 필수입니다";
    public static final String MONEY_DIVIDE_BY_ZERO = "0으로 나눌 수 없습니다";
    public static final String MONEY_AMOUNT_NULL = "금액은 null일 수 없습니다";
    public static final String MONEY_CURRENCY_REQUIRED = "통화 코드는 필수입니다";
    public static final String MONEY_CURRENCY_LENGTH = "통화 코드는 3자리여야 합니다";
    public static final String MONEY_OTHER_NULL = "비교 대상 금액은 null일 수 없습니다";

    public static final String TRADITIONAL_BOOK_IDS_NULL = "도서 ID 목록은 null일 수 없습니다.";
    public static final String TRADITIONAL_DB_CONNECT_FAILED = "DB 연결 실패";
    public static final String TRADITIONAL_BOOK_QUERY_FAILED = "책 조회 실패";
    public static final String TRADITIONAL_BOOK_LIST_QUERY_FAILED = "책 목록 조회 실패";
    public static final String TRADITIONAL_DB_CLOSE_FAILED = "DB 연결 종료 실패";

    public static String memberNotFound(Long id) {
        return "회원을 찾을 수 없습니다. ID: " + id;
    }

    public static String bookNotFound(Long id) {
        return "도서를 찾을 수 없습니다. ID: " + id;
    }

    public static String duplicateIsbn(String isbn) {
        return "이미 존재하는 ISBN입니다: " + isbn;
    }

    public static String bookNotAvailableById(Long id) {
        return "도서가 재고가 없습니다. ID: " + id;
    }

    public static String bookNotAvailableByTitle(String title) {
        return "도서가 재고가 없습니다. 제목: " + title;
    }

    public static String invalidIsbn(String isbn) {
        return "올바른 ISBN 형식이 아닙니다: " + isbn;
    }

    public static String invalidBookPrice(String detail) {
        return "잘못된 도서 가격입니다: " + detail;
    }

    public static String orderNotFound(Long id) {
        return "주문을 찾을 수 없습니다. ID: " + id;
    }

    public static String orderCancellationNotAllowed(Long orderId, Object status) {
        return String.format("주문을 취소할 수 없습니다. 주문 ID: %d, 현재 상태: %s", orderId, status);
    }

    public static String loanNotFound(Long id) {
        return "대여를 찾을 수 없습니다. ID: " + id;
    }

    public static String bookAlreadyLoaned(Long bookId) {
        return "도서가 이미 대여 중입니다. 도서 ID: " + bookId;
    }

    public static String bookNotLoanAvailable(Long bookId) {
        return "도서가 대여 불가능합니다. 도서 ID: " + bookId;
    }

    public static String loanLimitExceeded(Long memberId, int currentLoans, int maxLoans) {
        return String.format("대여 한도를 초과했습니다. 회원 ID: %d, 현재 대여: %d, 최대 대여: %d", memberId, currentLoans, maxLoans);
    }

    public static String overdueLoansExist(Long memberId) {
        return "연체 중인 도서가 있습니다. 먼저 반납해주세요. 회원 ID: " + memberId;
    }

    public static String alreadyReturned(Long loanId) {
        return "이미 반납된 대여입니다. 대여 ID: " + loanId;
    }

    public static String extensionLimitExceeded(int currentExtensions, int maxExtensions) {
        return String.format("연장 가능 횟수를 초과했습니다. (현재: %d, 최대: %d회)", currentExtensions, maxExtensions);
    }

    public static String extensionTooEarly(long daysUntilDue) {
        return String.format("반납 예정일 3일 전부터 연장 가능합니다. (반납 예정일까지 %d일 남음)", daysUntilDue);
    }

    public static String cannotDeleteActiveLoan(Long loanId, Object currentStatus) {
        return String.format("반납 완료된 대출만 삭제할 수 있습니다. 대출 ID: %d, 현재 상태: %s", loanId, currentStatus);
    }

    public static String paymentNotFound(Long id) {
        return "결제 정보를 찾을 수 없습니다. ID: " + id;
    }

    public static String refundNotFound(Long id) {
        return "환불 정보를 찾을 수 없습니다. ID: " + id;
    }

    public static String deliveryNotFound(Long id) {
        return "배송 정보를 찾을 수 없습니다. ID: " + id;
    }

    public static String invalidLoanPeriod(int days) {
        return "유효하지 않은 대여 기간입니다: " + days + "일. 사용 가능한 값: 7, 14, 21, 30";
    }

    public static String moneyCurrencyMismatch(String left, String right) {
        return String.format("통화가 다릅니다: %s vs %s", left, right);
    }
}