package com.example.spring.application.service;

import com.example.spring.domain.model.Order;
import com.example.spring.application.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MockEmailService implements EmailService {

    private static final String ADMIN_EMAIL = "admin@bookstore.com";

    @Override
    public void sendOrderConfirmation(Order order) {
        log.info("[EMAIL 발송] 수신자: {}, 제목: 주문이 완료되었습니다. (주문번호: {})",
                order.getMember().getEmail(), order.getId());
        log.info("[EMAIL 내용] {}님, 주문해주셔서 감사합니다. 총 결제금액: {}원",
                order.getMember().getName(), order.getTotalAmount());
    }

    @Override
    public void sendOrderShipped(Order order) {
        log.info("[EMAIL 발송] 수신자: {}, 제목: 주문하신 상품이 발송되었습니다. (주문번호: {})",
                order.getMember().getEmail(), order.getId());
    }

    @Override
    public void sendAdminAlert(String subject, String message) {
        log.warn("================ [ADMIN ALERT MAIL] ================");
        log.warn("수신자: {}", ADMIN_EMAIL);
        log.warn("제 목: [긴급] {}", subject);
        log.warn("내 용: {}", message);
        log.warn("조 치: 담당자는 내용을 확인 후 즉시 처리바랍니다.");
        log.warn("====================================================");
    }
}