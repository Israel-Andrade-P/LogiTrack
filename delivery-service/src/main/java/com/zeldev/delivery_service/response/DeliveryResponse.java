package com.zeldev.delivery_service.response;

import com.zeldev.delivery_service.enumeration.DeliveryStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record DeliveryResponse(
        String orderId,
        String courierId,
        String pickupAddress,
        String deliveryAddress,
        String packageDescription,
        BigDecimal packageWeight,
        DeliveryStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
