package com.zeldev.order_service.publisher;

import com.zeldev.order_service.event.OrderCreatedEvent;
import com.zeldev.order_service.event.OrderStatusUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {
    private static final String ORDER_CREATED_TOPIC = "order-created";
    private static final String STATUS_UPDATED_TOPIC = "status-updated";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCreatedEvent(OrderCreatedEvent event) {
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

    public void publishUpdatedEvent(OrderStatusUpdateEvent event) {
        kafkaTemplate.send(STATUS_UPDATED_TOPIC, event.orderId(), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Failed to publish status-updated for order {}", event.orderId(), error);
                        return;
                    }

                    var metadata = result.getRecordMetadata();
                    log.debug("Published status-updated for order {}, from status {} to {}, to partition {} at offset {}",
                            event.orderId(),
                            event.previousStatus(),
                            event.newStatus(),
                            metadata.partition(),
                            metadata.offset()
                    );
                });
    }
}
