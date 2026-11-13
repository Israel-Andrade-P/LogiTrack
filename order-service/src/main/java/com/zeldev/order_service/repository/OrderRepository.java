package com.zeldev.order_service.repository;

import com.zeldev.order_service.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("SELECT o FROM OrderEntity o WHERE o.publicId = :publicId AND o.customerId = :customerId")
    Optional<OrderEntity> findByPublicIdAndCustomerId(String publicId, String customerId);
}
