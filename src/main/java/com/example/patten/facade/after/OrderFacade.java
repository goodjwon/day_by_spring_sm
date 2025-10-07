package com.example.patten.facade.after;

import com.example.patten.facade.service.*;

public class OrderFacade {
    // 1. 복잡한 서브시스템에 대한 의존성을 파사드가 모두 가짐
    private final InventoryService inventoryService = new InventoryService();
    private final PaymentService paymentService = new PaymentService();
    private final ShippingService shippingService = new ShippingService();
    private final NotificationService notificationService = new NotificationService();

    // 2. 클라이언트에게는 단순화된 단일 메서드만 제공
    public void placeOrder(String item, String user, String address) {
        System.out.println("✅ [파사드] 주문 프로세스를 시작합니다...");
        // 3. 내부적으로 복잡한 절차를 순서에 맞게 조율
        inventoryService.checkStock(item);
        paymentService.processPayment(user);
        shippingService.arrangeShipping(address);
        notificationService.sendNotification(user);
        System.out.println("✅ [파사드] 모든 주문 절차가 완료되었습니다.");
    }
}
