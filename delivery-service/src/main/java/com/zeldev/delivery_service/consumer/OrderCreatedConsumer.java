package com.zeldev.delivery_service.consumer;

import com.zeldev.delivery_service.event.OrderCreatedEvent;
import com.zeldev.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {
    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = "order-created",
            containerFactory = "orderCreatedKafkaListenerContainerFactory"
    )
    public void consume(OrderCreatedEvent event) {
        log.info("Consuming message from broker for order {}", event.orderId());
        deliveryService.createDelivery(event);
    }
}
