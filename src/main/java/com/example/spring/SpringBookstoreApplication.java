package com.example.spring;

import com.example.spring.config.BookstoreProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy  // AOP 활성화
@EnableConfigurationProperties(BookstoreProperties.class)  // 설정 프로퍼티 활성화
public class SpringBookstoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBookstoreApplication.class, args);
	}

}
