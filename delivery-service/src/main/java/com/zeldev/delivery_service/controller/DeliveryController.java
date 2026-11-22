package com.zeldev.delivery_service.controller;

import com.zeldev.delivery_service.response.DeliveryResponse;
import com.zeldev.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService service;

    @GetMapping("/mine")
    @PreAuthorize("hasRole('DOOR_DASHER')")
    public ResponseEntity<Page<DeliveryResponse>> getMyDeliveries(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getCourierDeliveries(jwt.getSubject(), pageable));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('DOOR_DASHER')")
    public ResponseEntity<DeliveryResponse> getMyDelivery(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String orderId
    ) {
        return ResponseEntity.ok(service.getCourierDelivery(orderId, jwt.getSubject()));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DeliveryResponse>> getAllDeliveries(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getAllDeliveries(pageable));
    }
}
