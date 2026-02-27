package com.example.spring.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 공통
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex instanceof MemberException.MemberNotFoundException ||
                ex instanceof BookException.BookNotFoundException ||
                ex instanceof OrderException.OrderNotFoundException ||
                ex instanceof LoanException.LoanNotFoundException ||
                ex instanceof PaymentException.PaymentNotFoundException ||
                ex instanceof RefundException.RefundNotFoundException ||
                ex instanceof DeliveryException.DeliveryNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof MemberException.DuplicateEmailException) {
            status = HttpStatus.BAD_REQUEST;
        }
        String message = ex.getMessage();
        if (ex instanceof MemberException.MemberNotFoundException memberNotFound && memberNotFound.getMemberId() != null) {
            message = ErrorMessages.memberNotFound(memberNotFound.getMemberId());
        }
        if (ex instanceof MemberException.DuplicateEmailException
                && "PUT".equalsIgnoreCase(request.getMethod())) {
            message = message.replace("존재하는", "사용 중인");
        }
        log.warn("Business exception: code={}, message={}", ex.getErrorCode(), message);
        return buildErrorResponse(status, ex.getErrorCode(), message, request.getRequestURI());
    }

    // @Valid 바디 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> ErrorResponse.FieldError.builder()
                        .field(err.getField())
                        .rejectedValue(safeToString(err.getRejectedValue()))
                        .message(err.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        log.debug("Validation failed: {} errors", fieldErrors.size());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                ErrorMessages.VALIDATION_ERROR, request.getRequestURI(), fieldErrors);
    }

    // 폼/쿼리 파라미터 바인딩 실패
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> ErrorResponse.FieldError.builder()
                        .field(err.getField())
                        .rejectedValue(safeToString(err.getRejectedValue()))
                        .message(err.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "BINDING_FAILED",
                ErrorMessages.BINDING_FAILED, request.getRequestURI(), fieldErrors);
    }

    // @Validated on @RequestParam, @PathVariable 등
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {
        List<ErrorResponse.FieldError> violations = ex.getConstraintViolations().stream()
                .map(v -> ErrorResponse.FieldError.builder()
                        .field(v.getPropertyPath().toString())
                        .rejectedValue(safeToString(v.getInvalidValue()))
                        .message(v.getMessage())
                        .build())
                .collect(Collectors.toList());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION",
                ErrorMessages.CONSTRAINT_VIOLATION, request.getRequestURI(), violations);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleBadRequests(Exception ex, HttpServletRequest request) {
        log.debug("Bad request: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex,
                                                                       HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "AUTHENTICATION_FAILED",
                ErrorMessages.AUTHENTICATION_FAILED,
                request.getRequestURI());
    }

    // 404 Not Found (리소스 없음) - 요청 출처 추적
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            org.springframework.web.servlet.resource.NoResourceFoundException ex,
            HttpServletRequest request) {

        // 요청 정보 상세 로깅
        log.warn("=== 404 Not Found - 요청 출처 추적 ===");
        log.warn("URI: {}", request.getRequestURI());
        log.warn("Method: {}", request.getMethod());
        log.warn("Remote Address: {}", request.getRemoteAddr());
        log.warn("User-Agent: {}", request.getHeader("User-Agent"));
        log.warn("Referer: {}", request.getHeader("Referer"));
        log.warn("Origin: {}", request.getHeader("Origin"));
        log.warn("X-Requested-With: {}", request.getHeader("X-Requested-With"));

        // 모든 헤더 출력
        log.warn("All Headers:");
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.warn("  {}: {}", headerName, request.getHeader(headerName));
        }
        log.warn("=====================================");

        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND",
                ErrorMessages.RESOURCE_NOT_FOUND, request.getRequestURI());
    }

    // 알 수 없는 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                ErrorMessages.INTERNAL_SERVER_ERROR, request.getRequestURI());
    }

    // Helper
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String code, String message, String path) {
        return buildErrorResponse(status, code, message, path, List.of());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String code, String message,
                                                             String path, List<ErrorResponse.FieldError> fieldErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .errorCode(code)
                .message(message)
                .path(path)
                .fieldErrors(fieldErrors == null || fieldErrors.isEmpty() ? null : fieldErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private String safeToString(Object value) {
        if (value == null) return null;
        try {
            return String.valueOf(value);
        } catch (Exception e) {
            return "<unprintable>";
        }
    }
}