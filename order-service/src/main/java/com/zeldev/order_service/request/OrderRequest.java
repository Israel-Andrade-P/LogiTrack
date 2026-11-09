package com.zeldev.order_service.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderRequest(
        @NotBlank String pickupAddress,
        @NotBlank String deliveryAddress,
        String packageDescription,
        @Positive BigDecimal packageWeight
) {}
