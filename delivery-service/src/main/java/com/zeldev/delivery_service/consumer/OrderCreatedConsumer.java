package com.zeldev.delivery_service.consumer;

import com.zeldev.delivery_service.event.OrderCreatedEvent;
import com.zeldev.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {
    private final DeliveryService deliveryService;

    @KafkaListener(topics = "order-created")
    public void consume(OrderCreatedEvent event) {
        deliveryService.createDelivery(event);
    }
}
