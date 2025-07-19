package com.example.spring.repository.imple;

import com.example.spring.entity.Order;
import com.example.spring.repository.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long>, OrderRepository {
    // 추가적인 쿼리 메서드를 필요에 따라 선언 가능
    // 예: List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
}
