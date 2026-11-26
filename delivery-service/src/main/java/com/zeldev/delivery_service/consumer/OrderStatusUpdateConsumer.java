package com.zeldev.delivery_service.consumer;

import com.zeldev.delivery_service.event.OrderStatusUpdateEvent;
import com.zeldev.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusUpdateConsumer {
    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = "status-updated",
            containerFactory = "orderStatusUpdateKafkaListenerContainerFactory"
    )
    public void consume(OrderStatusUpdateEvent event) {
        log.info("Consuming status update for order {}: {} -> {}",
                event.orderId(), event.previousStatus(), event.newStatus());
        deliveryService.cancelDelivery(event);
    }
}
