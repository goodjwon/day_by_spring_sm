package com.example.patten.observer.before;

class InventoryService {
    public void updateStock(String productId) {
        System.out.println("[재고] 상품 ID: " + productId + "의 재고를 차감합니다.");
    }
}

class ShippingService {
    public void prepareShipping(String address) {
        System.out.println("[배송] 주소: " + address + "로 배송을 준비합니다.");
    }
}

public class OrderService {
    private final InventoryService inventoryService =  new InventoryService();
    private final ShippingService shippingService = new ShippingService();

    public void placeOrder(String productId, String address) {
        System.out.println("--- 주문 처리를 시작합니다 ---");
        //비즈니스 로직 수행...
        System.out.println("주문이 성공적으로 완료되었습니다.");

        // 다른 서비스들을 직접 호출 (간한 결합)
        inventoryService.updateStock(productId);
        shippingService.prepareShipping(address);
        System.out.println("----------------------\n");
    }
}
