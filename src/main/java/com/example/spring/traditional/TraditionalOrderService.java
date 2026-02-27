package com.example.spring.traditional;

import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.Book;
import com.example.spring.domain.model.Order;
import com.example.spring.exception.BookException;
import com.example.spring.exception.ErrorMessages;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 4. 비즈니스 로직 - 모든 문제점이 집약된 클래스
public class TraditionalOrderService {
    private final TraditionalBookRepository bookRepository;
    private final TraditionalEmailService emailService;
    private final TraditionalLoggingService loggingService;

    public TraditionalOrderService() {
        // 강한 결합: 구체 클래스에 직접 의존
        // 테스트 시 Mock 객체 사용 불가
        this.bookRepository = new TraditionalBookRepository();
        this.emailService = new TraditionalEmailService();
        this.loggingService = new TraditionalLoggingService();
    }

    public Order createOrder(List<Long> bookIds) {
        // 로깅 코드가 비즈니스 로직과 섞임
        loggingService.log("주문 생성 시작 - 도서 ID: " + bookIds);

        if (bookIds == null) {
            throw new IllegalArgumentException(ErrorMessages.TRADITIONAL_BOOK_IDS_NULL);
        }

        long startTime = System.currentTimeMillis();

        try {
            // 1. 도서 조회 및 검증
            List<Book> books = new ArrayList<>();
            Money total = Money.zero();

            for (Long bookId : bookIds) {
                Book book = bookRepository.findById(bookId);
                if (book == null) {
                    loggingService.error("존재하지 않는 도서 ID: " + bookId, null);
                    throw new BookException.BookNotFoundException(bookId);
                }
                books.add(book);
                total = total.add(book.getPrice());
            }

            // 2. 주문 생성
            Order order = Order.builder()
                    .totalAmount(total)
                    .orderDate(LocalDateTime.now())
                    .build();

            // 참고: 실제로는 DB 저장 후 ID가 생성됨
            // 이 예제에서는 ID 설정 없이 진행

            // 3. 이메일 발송
            emailService.sendOrderConfirmation(order);

            long endTime = System.currentTimeMillis();
            loggingService.log("주문 생성 완료 - 실행 시간: " + (endTime - startTime) + "ms");

            return order;

        } catch (Exception e) {
            // 전체 스택 트레이스 대신 예외 타입과 메시지만 간결하게 로깅
            loggingService.error(String.format("주문 생성 실패 - 원인: [%s] %s", e.getClass().getSimpleName(), e.getMessage()), null);
            throw e;
        }
    }

    // 자원 정리 메서드 - 개발자가 직접 호출해야 함
    public void cleanup() {
        if (bookRepository != null) {
            bookRepository.close();
        }
    }
}