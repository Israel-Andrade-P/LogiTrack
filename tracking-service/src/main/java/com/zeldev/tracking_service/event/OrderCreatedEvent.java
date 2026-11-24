package com.zeldev.tracking_service.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        String orderId,
        String customerId,
        String pickupAddress,
        String deliveryAddress,
        String packageDescription,
        BigDecimal packageWeight,
        Instant createdAt
) {
}
