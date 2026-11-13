package com.zeldev.order_service.handler;

import lombok.Builder;

@Builder
public record ErrorResponse(int code, String message) {}
