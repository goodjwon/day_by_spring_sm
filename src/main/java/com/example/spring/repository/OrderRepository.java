package com.example.spring.repository;

import com.example.spring.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    // 편의 메서드 - Optional을 처리하지 않고 직접 Order 반환
    default Order findByIdOrThrow(Long id){
        return findById(id).orElse(null);
    }

    @Query("SELECT o FROM Order o WHERE o.member.id = :memberId ORDER BY o.orderDate DESC")
    List<Order> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT o FROM Order o JOIN OrderItem oi ON o.id = oi.order.id WHERE oi.book.id = :bookId ORDER BY o.orderDate DESC")
    List<Order> findByOrderItemsBookId(@Param("bookId") Long bookId);
}
