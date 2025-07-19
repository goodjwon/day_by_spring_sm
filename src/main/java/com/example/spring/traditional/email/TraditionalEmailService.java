package com.example.spring.traditional.email;

import com.example.spring.entity.Order;

public class TraditionalEmailService {
    private String smtpHost;
    private int smtpPort;

    public TraditionalEmailService() {
        // 하드코딩된 SMTP 설정
        this.smtpHost = "localhost";
        this.smtpPort = 587;
        System.out.println("SMTP 서비스 초기화: " + smtpHost + ":" + smtpPort);
    }

    public void sendOrderConfirmation(Order order) {
        // 실제 이메일 발송 로직 (여기서는 콘솔 출력)
        System.out.println("=== 주문 확인 이메일 발송 ===");
        System.out.println("주문 ID: " + order.getId());
        System.out.println("총 금액: " + order.getTotalAmount());
        System.out.println("주문 일시: " + order.getOrderDate());
        System.out.println("SMTP 서버: " + smtpHost + ":" + smtpPort);
        System.out.println("이메일 발송 완료");
    }
}
