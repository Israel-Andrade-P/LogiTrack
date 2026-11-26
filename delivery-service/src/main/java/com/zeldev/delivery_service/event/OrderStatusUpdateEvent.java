package com.zeldev.delivery_service.event;

import com.zeldev.delivery_service.enumeration.OrderStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record OrderStatusUpdateEvent(
        String orderId,
        String customerId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        Instant occurredAt
) {
}
