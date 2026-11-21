package com.zeldev.delivery_service.repository;

import com.zeldev.delivery_service.entity.DeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long> {
    boolean existsByOrderId(String orderId);
}
