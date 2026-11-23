package com.zeldev.delivery_service.utils;

import com.zeldev.delivery_service.entity.DeliveryEntity;
import com.zeldev.delivery_service.enumeration.DeliveryStatus;
import com.zeldev.delivery_service.event.OrderCreatedEvent;
import com.zeldev.delivery_service.response.DeliveryResponse;

import java.time.Instant;

public final class DeliveryMapper {
    private DeliveryMapper(){}

    public static DeliveryEntity toEntity(OrderCreatedEvent event, String courierId, DeliveryStatus status) {
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

    public static DeliveryResponse toResponse(DeliveryEntity delivery) {
        return new DeliveryResponse(
                delivery.getOrderId(),
                delivery.getCourierId(),
                delivery.getPickupAddress(),
                delivery.getDeliveryAddress(),
                delivery.getPackageDescription(),
                delivery.getPackageWeight(),
                delivery.getStatus(),
                delivery.getCreatedAt(),
                delivery.getUpdatedAt()
        );
    }
}
