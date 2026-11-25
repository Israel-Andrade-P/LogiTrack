package com.zeldev.tracking_service.service;

import com.zeldev.tracking_service.entity.TrackingEntity;
import com.zeldev.tracking_service.enumeration.TrackingEventType;
import com.zeldev.tracking_service.event.OrderCreatedEvent;
import com.zeldev.tracking_service.event.OrderStatusUpdateEvent;
import com.zeldev.tracking_service.repository.TrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackingService {
    private final TrackingRepository trackingRepository;

    public void recordOrderCreated(OrderCreatedEvent event) {
        var trackingEvent = TrackingEntity.builder()
                .customerId(event.customerId())
                .orderId(event.orderId())
                .eventType(TrackingEventType.CREATED)
                .occurredAt(event.createdAt())
                .description("Order created")
                .build();

        trackingRepository.save(trackingEvent);
    }

    public void recordOrderStatusUpdate(OrderStatusUpdateEvent event) {
        var trackingEvent = TrackingEntity.builder()
                .customerId(event.customerId())
                .orderId(event.orderId())
                .eventType(event.newStatus())
                .occurredAt(event.occurredAt())
                .description("Order status changed from " + event.previousStatus() + " to " + event.newStatus())
                .build();

        trackingRepository.save(trackingEvent);
    }
}
