package com.zeldev.order_service.response;

import com.zeldev.order_service.enumeration.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String orderId,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record OrderItemResponse(
            String productId,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}
}
