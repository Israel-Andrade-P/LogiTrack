package com.zeldev.order_service.request;

import com.zeldev.order_service.enumeration.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(@NotNull OrderStatus status) {
}
