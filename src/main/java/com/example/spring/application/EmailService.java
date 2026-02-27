package com.example.spring.application;

import com.example.spring.domain.model.Order;

public interface EmailService {
    void sendOrderConfirmation(Order order);
    void sendOrderShipped(Order order);

    /**
     * 관리자에게 시스템 경고/알림 메일을 발송합니다.
     * @param subject 메일 제목
     * @param message 메일 내용 (발생 원인, 조치 필요 사항 등)
     */
    void sendAdminAlert(String subject, String message);
}