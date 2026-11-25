package com.zeldev.order_service.utils;

import com.zeldev.order_service.entity.OrderEntity;
import com.zeldev.order_service.enumeration.OrderStatus;
import com.zeldev.order_service.event.OrderCreatedEvent;
import com.zeldev.order_service.event.OrderStatusUpdateEvent;

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

    public static OrderStatusUpdateEvent createStatusEvent(OrderEntity entity, OrderStatus prevStatus) {
        return OrderStatusUpdateEvent.builder()
                .orderId(entity.getPublicId())
                .customerId(entity.getCustomerId())
                .previousStatus(prevStatus)
                .newStatus(entity.getStatus())
                .occurredAt(entity.getUpdatedAt())
                .build();
    }
}
