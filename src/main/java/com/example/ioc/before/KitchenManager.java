package com.example.ioc.before;

class ChefWithIoC {
    private Beef beef;
    private Onion onion;
    private Salt salt;

    public ChefWithIoC(Beef beef, Onion onion, Salt salt) {
        this.beef = beef;
        this.onion = onion;
        this.salt = salt;
        System.out.println("👨‍🍳 Chef가 준비된 재료를 받았습니다.");
    }

    public void cook() {
        System.out.println("🍳 " + beef.getName() + ", " + onion.getName() +
                ", " + salt.getName() + "으로 요리를 시작합니다.");
    }
}

class KitchenManager {
    public ChefWithIoC createChef() {
        System.out.println("🏪 KitchenManager가 재료들을 준비합니다...");

        Beef beef = new Beef();
        Onion onion = new Onion();
        Salt salt = new Salt();

        System.out.println("📦 재료 준비 완료!");

        return new ChefWithIoC(beef, onion, salt);
    }

    public static void main(String[] args) {
        KitchenManager kitchenManager = new KitchenManager();
        ChefWithIoC chef = kitchenManager.createChef();
        chef.cook();
    }
}
