package com.zeldev.order_service.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrderRequest(@NotEmpty List<@Valid OrderItemRequest> items) {
    public record OrderItemRequest(
            @NotNull String productId,
            @Positive int quantity
    ) {}
}
