package com.example.ioc.after;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
class SpringBeef {
    public String getName() {
        return "Spring 소고기";
    }
}

@Component
class SpringOnion {
    public String getName() {
        return "Spring 양파";
    }
}

@Component
class SpringSalt {
    public String getName() {
        return "Spring 소금";
    }
}

@Service
class SpringChef {
    private final SpringBeef beef;
    private final SpringOnion onion;
    private final SpringSalt salt;

    @Autowired
    public SpringChef(SpringBeef beef, SpringOnion onion, SpringSalt salt) {
        this.beef = beef;
        this.onion = onion;
        this.salt = salt;
        System.out.println("🌱 Spring이 Chef에게 재료를 자동 주입했습니다!");
    }

    public void cook() {
        System.out.println("🍳 " + beef.getName() + ", " + onion.getName() +
                ", " + salt.getName() + "으로 요리를 시작합니다.");
    }

    @Configuration
    @ComponentScan
    class KitchenConfig {

    }

    public static void main(String[] args) {
        System.out.println("=== Spring IoC 적용 실행 ===");
        ApplicationContext context = new AnnotationConfigApplicationContext(KitchenConfig.class);
        SpringChef chef = context.getBean(SpringChef.class);
        chef.cook();
    }
}
