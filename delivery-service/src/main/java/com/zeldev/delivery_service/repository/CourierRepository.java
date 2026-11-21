package com.zeldev.delivery_service.repository;

import com.zeldev.delivery_service.entity.CourierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourierRepository extends JpaRepository<CourierEntity, String> {
    Optional<CourierEntity> findFirstByAvailableTrue();
}
