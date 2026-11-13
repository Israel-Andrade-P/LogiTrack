package com.zeldev.order_service.utils;

import com.zeldev.order_service.entity.OrderEntity;
import com.zeldev.order_service.event.OrderCreatedEvent;

public final class OrderEvent {
    private OrderEvent() {
    }

    public static OrderCreatedEvent createEvent(OrderEntity entity) {
        return OrderCreatedEvent.builder()
                .orderId(entity.getPublicId())
                .customerId(entity.getCustomerId())
                .pickupAddress(entity.getPickupAddress())
                .deliveryAddress(entity.getDeliveryAddress())
                .packageDescription(entity.getPackageDescription())
                .packageWeight(entity.getPackageWeight())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
