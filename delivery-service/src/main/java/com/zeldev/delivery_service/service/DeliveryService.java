package com.zeldev.delivery_service.service;

import com.zeldev.delivery_service.entity.CourierEntity;
import com.zeldev.delivery_service.entity.DeliveryEntity;
import com.zeldev.delivery_service.enumeration.DeliveryStatus;
import com.zeldev.delivery_service.event.OrderCreatedEvent;
import com.zeldev.delivery_service.exception.NoCourierAvailableException;
import com.zeldev.delivery_service.repository.CourierRepository;
import com.zeldev.delivery_service.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static com.zeldev.delivery_service.enumeration.DeliveryStatus.ASSIGNED;
import static com.zeldev.delivery_service.enumeration.DeliveryStatus.PENDING_ASSIGNMENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final CourierRepository courierRepository;

    @Transactional
    public void createDelivery(OrderCreatedEvent event) {
        if (checkByOrderId(event.orderId())) {
            log.info("Delivery with order ID {} already exists", event.orderId());
            return;
        }

        var deliveryStatus = ASSIGNED;

        var courier = findAvailableCourier();

        if (courier.isEmpty()) {
            deliveryStatus = PENDING_ASSIGNMENT;
            deliveryRepository.save(toEntity(event, null, deliveryStatus));
            return;
        }

        deliveryRepository.save(toEntity(event, courier.get().getUserId(), deliveryStatus));

        courier.get().setAvailable(false);
    }

    private boolean checkByOrderId(String orderId) {
        return deliveryRepository.existsByOrderId(orderId);
    }

    private Optional<CourierEntity> findAvailableCourier() {
        return courierRepository.findFirstByAvailableTrue();
    }

    private DeliveryEntity toEntity(OrderCreatedEvent event, String courierId, DeliveryStatus status) {
        var now = Instant.now();

        return DeliveryEntity.builder()
                .orderId(event.orderId())
                .courierId(courierId)
                .pickupAddress(event.pickupAddress())
                .deliveryAddress(event.deliveryAddress())
                .packageDescription(event.packageDescription())
                .packageWeight(event.packageWeight())
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
