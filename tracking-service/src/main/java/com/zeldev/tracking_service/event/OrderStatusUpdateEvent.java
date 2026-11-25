package com.zeldev.tracking_service.event;

import com.zeldev.tracking_service.enumeration.TrackingEventType;

import java.time.Instant;

public record OrderStatusUpdateEvent(
        String orderId,
        String customerId,
        TrackingEventType previousStatus,
        TrackingEventType newStatus,
        Instant occurredAt
) {
}
