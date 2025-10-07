package com.example.patten.facade.after;

public class OrderClient {
    private final OrderFacade orderFacade =  new OrderFacade();

    public void placeOrder() {
        System.out.println("--- 클라이언트가 파사드를 통해 주문합니다. ---");
        // 클라이언트는 복잡한 과정을 모르고, 파사드에 위임하기만 하면 됨
        orderFacade.placeOrder("노트북", "홍길동", "서울시 강남구");
        System.out.println("----------------------------------------");
    }
}
