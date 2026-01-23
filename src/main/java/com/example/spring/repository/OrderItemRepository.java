package com.example.spring.repository;

import com.example.spring.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, JpaSpecificationExecutor<OrderItem> {
    @Query("SELECT oi FROM OrderItem oi JOIN Order o ON oi.order.id = o.id WHERE oi.order.id = :orderId ORDER BY oi.id DESC ")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.id = :orderItemId ORDER BY oi.id DESC ")
    List<OrderItem> findByOrderItemId(Long orderItemId);
}
