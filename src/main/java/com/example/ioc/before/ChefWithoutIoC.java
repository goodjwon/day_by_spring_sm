package com.example.ioc.before;

class Beef {
    public String getName() {
        return "소고기";
    }
}

class Onion {
    public String getName() {
        return "양파";
    }
}

class Salt {
    public String getName() {
        return "소금";
    }
}

class ChefWithoutIoC {
    private Beef beef;
    private Onion onion;
    private Salt salt;

    public ChefWithoutIoC() {
        this.beef = new Beef();
        this.onion = new Onion();
        this.salt = new Salt();
        System.out.println("\uD83D\uDC68\u200D\uD83C\uDF73 Chef가 직접 재료를 준비했습니다.");
    }

    public void cook() {
        System.out.println("🍳 " + beef.getName() + ", " + onion.getName() +
                ", " + salt.getName() + "으로 요리를 시작합니다.");
    }

    public static void main(String[] args) {
        ChefWithoutIoC chef = new ChefWithoutIoC();
        chef.cook();
    }
}
