package com.zeldev.order_service.publisher;

import com.zeldev.order_service.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {
    private static final String ORDER_CREATED_TOPIC = "order-created";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void publish(OrderCreatedEvent event) {
        kafkaTemplate.send(ORDER_CREATED_TOPIC, event.orderId(), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Failed to publish order-created for order {}", event.orderId(), error);
                        return;
                    }

                    var metadata = result.getRecordMetadata();
                    log.debug("Published order-created for order {} to partition {} at offset {}",
                            event.orderId(),
                            metadata.partition(),
                            metadata.offset()
                    );
        });
    }
}
