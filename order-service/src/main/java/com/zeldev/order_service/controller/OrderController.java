package com.zeldev.order_service.controller;

import com.zeldev.order_service.request.OrderRequest;
import com.zeldev.order_service.response.OrderResponse;
import com.zeldev.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<OrderResponse> placeOrder(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid OrderRequest request) {
        String userId = jwt.getSubject();
        var orderResponse = orderService.place(request, userId);
        return ResponseEntity.status(CREATED).body(orderResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") String orderId) {
        return ResponseEntity.status(OK).body(orderService.getOrder(orderId, jwt.getSubject()));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrdersByCustomer(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.status(OK).body(orderService.getOrdersByCustomer(jwt.getSubject(), pageable));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.status(OK).body(orderService.getOrders(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> deleteOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") String orderId) {
        return ResponseEntity.status(OK).body(orderService.deleteOrder(orderId, jwt.getSubject()));
    }
}
