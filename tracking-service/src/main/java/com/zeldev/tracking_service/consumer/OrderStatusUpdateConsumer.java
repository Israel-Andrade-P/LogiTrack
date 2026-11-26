package com.zeldev.tracking_service.consumer;

import com.zeldev.tracking_service.event.OrderStatusUpdateEvent;
import com.zeldev.tracking_service.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusUpdateConsumer {
    private final TrackingService trackingService;

    @KafkaListener(
            topics = "status-updated",
            containerFactory = "orderStatusUpdateKafkaListenerContainerFactory"
    )
    public void consume(OrderStatusUpdateEvent event) {
        log.info("Consuming status-updated event for order {}", event.orderId());
        trackingService.recordOrderStatusUpdate(event);
    }
}
