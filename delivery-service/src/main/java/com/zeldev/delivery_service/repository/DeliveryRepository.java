package com.zeldev.delivery_service.repository;

import com.zeldev.delivery_service.entity.DeliveryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long> {
    boolean existsByOrderId(String orderId);

    Optional<DeliveryEntity> findByOrderId(String orderId);

    Optional<DeliveryEntity> findByOrderIdAndCourierId(String orderId, String courierId);

    Page<DeliveryEntity> findAllByCourierId(String courierId, Pageable pageable);
}
