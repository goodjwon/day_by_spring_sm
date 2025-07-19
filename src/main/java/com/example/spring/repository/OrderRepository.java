package com.example.spring.repository;

import com.example.spring.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findAll();

    // 편의 메서드 - Optional을 처리하지 않고 직접 Order 반환
    default Order findByIdOrThrow(Long id){
        return findById(id).orElse(null);
    }
}
