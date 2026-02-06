package com.example.spring.repository;

import com.example.spring.entity.Order;
import com.example.spring.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    Order findOrderById(@Param("orderId") Long orderId);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.id DESC")
    List<Order> findByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate " +
            "AND :endDate ORDER BY o.id DESC")
    List<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.totalAmount BETWEEN :min AND :max ORDER BY o.id DESC")
    List<Order> findByTotalAmountBetween(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query("SELECT o FROM Order o JOIN o.orderItems oi WHERE oi.book.id = :bookId ORDER BY o.id DESC")
    List<Order> findByOrderItems_Book_Id(@Param("bookId") Long bookId);
}
