package com.example.patten.facade.before;


import com.example.patten.facade.service.*;

// 클라이언트 (여러 서비스에 직접 의존하고, 처리 순서를 직접 관리)
public class OrderClient {
    private final InventoryService inventoryService = new InventoryService();
    private final PaymentService paymentService = new PaymentService();
    private final ShippingService shippingService = new ShippingService();
    private final NotificationService notificationService = new NotificationService();
    private final CouponService couponService = new CouponService();

    public void placeOrder() {
        System.out.println("--- 클라이언트가 직접 주문을 처리합니다. ---");
        // 클라이언트가 직접 서브시스템의 복잡한 절차를 알고 호출해야 함
        inventoryService.checkStock("노트북");
        paymentService.processPayment("홍길동");
        shippingService.arrangeShipping("서울시 강남구");
        notificationService.sendNotification("홍길동");
        couponService.checkStock("할인 쿠폰");
        System.out.println("--------------------------------------");

        /** todo
         * 이게 무슨 장점이 있지??
         */
    }
}
