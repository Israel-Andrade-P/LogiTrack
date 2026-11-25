package com.zeldev.tracking_service.entity;

import com.zeldev.tracking_service.enumeration.TrackingEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "tracking")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEntity {
    @Id
    private String id;
    private String customerId;
    private String orderId;
    private TrackingEventType eventType;
    private Instant occurredAt;
    private String description;
}
