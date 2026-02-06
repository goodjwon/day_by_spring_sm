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

    @Query("SELECT oi FROM OrderItem oi JOIN oi.book b WHERE b.id = :bookId")
    List<OrderItem> findByBookId(@Param("bookId") Long bookId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND oi.book.id = :bookId")
    List<OrderItem> findByOrderIdAndBookId(@Param("orderId") Long orderId, @Param("bookId") Long bookId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.id = :orderItemId")
    OrderItem findOrderItemById(@Param("orderItemId") Long orderItemId);
}
