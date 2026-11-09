package com.zeldev.order_service.response;

import com.zeldev.order_service.enumeration.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String orderId,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
