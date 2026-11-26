package com.zeldev.tracking_service.consumer;

import com.zeldev.tracking_service.event.OrderCreatedEvent;
import com.zeldev.tracking_service.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {
    private final TrackingService trackingService;

    @KafkaListener(
            topics = "order-created",
            containerFactory = "orderCreatedKafkaListenerContainerFactory"
    )
    public void consume(OrderCreatedEvent event) {
        log.info("Consuming order-created for order {}", event.orderId());
        trackingService.recordOrderCreated(event);
    }
}
