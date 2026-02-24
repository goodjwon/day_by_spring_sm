package com.example.spring.domain.repository;

import com.example.spring.domain.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);
    List<OrderItem> findByBookId(Long bookId);
    List<OrderItem> findByOrderIdAndBookId(Long orderId, Long bookId);

    default OrderItem findOrderItemById(Long id) {
        return findById(id).orElse(null);
    }
}