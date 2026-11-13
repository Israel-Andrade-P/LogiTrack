package com.zeldev.order_service.utils;

import com.zeldev.order_service.entity.OrderEntity;
import com.zeldev.order_service.request.OrderRequest;
import com.zeldev.order_service.response.OrderResponse;

import java.time.Instant;

import static com.zeldev.order_service.enumeration.OrderStatus.CREATED;

public final class OrderMapper {
    private OrderMapper() {
    }

    public static OrderEntity toEntity(OrderRequest request, String userId) {
        return OrderEntity.builder()
                .customerId(userId)
                .pickupAddress(request.pickupAddress())
                .deliveryAddress(request.deliveryAddress())
                .packageDescription(request.packageDescription())
                .packageWeight(request.packageWeight())
                .status(CREATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static OrderResponse toResponse(OrderEntity entity) {
        return OrderResponse.builder()
                .orderId(entity.getPublicId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
