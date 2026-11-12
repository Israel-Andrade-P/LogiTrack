package com.zeldev.order_service.service;

import com.zeldev.order_service.entity.OrderEntity;
import com.zeldev.order_service.event.OrderCreatedEvent;
import com.zeldev.order_service.publisher.OrderEventPublisher;
import com.zeldev.order_service.repository.OrderRepository;
import com.zeldev.order_service.request.OrderRequest;
import com.zeldev.order_service.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.zeldev.order_service.enumeration.OrderStatus.CREATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEventPublisher publisher;
    /*
    Order lifecycles:
    CREATED → COLLECTED → IN_TRANSIT → DELIVERED
    CREATED -> CANCELLED
     */

    public OrderResponse place(OrderRequest request, String userId) {
        var entity = orderRepository.save(toEntity(request, userId));
        var event = createEvent(entity);

        log.info("Publishing a event to kafka broker for order: {}", entity.getPublicId());
        publisher.publish(event);

        return toResponse(entity);
    }

    private OrderEntity toEntity(OrderRequest request, String userId) {
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

    private OrderResponse toResponse(OrderEntity entity) {
        return OrderResponse.builder()
                .orderId(entity.getPublicId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private OrderCreatedEvent createEvent(OrderEntity entity) {
        return OrderCreatedEvent.builder()
                .orderId(entity.getPublicId())
                .customerId(entity.getCustomerId())
                .pickupAddress(entity.getPickupAddress())
                .deliveryAddress(entity.getDeliveryAddress())
                .packageDescription(entity.getPackageDescription())
                .packageWeight(entity.getPackageWeight())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
