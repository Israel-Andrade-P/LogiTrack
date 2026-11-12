package com.zeldev.order_service.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record OrderCreatedEvent(
        String orderId,
        String customerId,
        String pickupAddress,
        String deliveryAddress,
        String packageDescription,
        BigDecimal packageWeight,
        Instant createdAt
) {}
