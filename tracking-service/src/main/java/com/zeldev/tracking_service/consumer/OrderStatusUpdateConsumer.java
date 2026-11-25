package com.zeldev.tracking_service.consumer;

import com.zeldev.tracking_service.event.OrderStatusUpdateEvent;
import com.zeldev.tracking_service.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusUpdateConsumer {
    private final TrackingService trackingService;

    @KafkaListener(
            topics = "status-updated",
            containerFactory = "orderStatusUpdateKafkaListenerContainerFactory"
    )
    public void consume(OrderStatusUpdateEvent event) {
        trackingService.recordOrderStatusUpdate(event);
    }
}
