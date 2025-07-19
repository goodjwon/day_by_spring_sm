package com.example.patten.strategy.before;

public class PaymentService {
    public void processPayment(String paymentMethod, int amount) {
        if ("creditCard".equals(paymentMethod)) {
            System.out.println(" [처리] 신용카드로 " + amount + "원 결제를 시도합니다.");
            System.out.println(" [성공] 신용카드 결제가 완료되었습니다.");
        } else if ("bankTransfer".equals(paymentMethod)) {
            System.out.println(" [처리] 계좌이체로 " + amount + "원 결제를 시도합니다.");
            System.out.println(" [성공] 계좌이체 결제가 완료되었습니다.");
        } else if ("mobile".equals(paymentMethod)) {
            System.out.println(" [처리] 모바일로 " + amount + "원 결제를 시도합니다.");
            System.out.println(" [성공] 모바일 결제가 완료되었습니다.");
        } else {
            System.out.println(" [실패] 지원하지 않는 결제 방식입니다.");
        }
    }
}
