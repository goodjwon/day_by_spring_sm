package com.example.spring.repository;

import com.example.spring.entity.Delivery;
import com.example.spring.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long>, JpaSpecificationExecutor<Delivery> {
    @Query("SELECT d FROM Delivery d JOIN FETCH d.order WHERE d.order.id = :orderId")
    Optional<Delivery> findByOrderId(Long orderId);

    @Query("SELECT d FROM Delivery d WHERE d.trackingNumber = :trackingNumber")
    Optional<Delivery> findByTrackingNumber(@Param("trackingNumber") String trackingNumber);

    @Query("SELECT d FROM Delivery d WHERE d.status = :status")
    List<Delivery> findByStatus(@Param("status")DeliveryStatus status);

    @Query("SELECT d FROM Delivery d WHERE d.recipientName LIKE CONCAT('%', :recipientName, '%')")
    List<Delivery> findByRecipientNameContaining(@Param("recipientName") String recipientName);

    @Query("SELECT d FROM Delivery d WHERE d.courierCompany = :courierCompany")
    List<Delivery> findByCourierCompany(@Param("courierCompany") String courierCompany);

    @Query("SELECT d FROM Delivery d WHERE d.status = :status AND d.order.id = :orderId")
    List<Delivery> findByStatusAndOrderId(@Param("status") DeliveryStatus status, @Param("orderId") Long orderId);
}
