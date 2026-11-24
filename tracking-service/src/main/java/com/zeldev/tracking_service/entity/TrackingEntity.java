package com.zeldev.tracking_service.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tracking")
public class TrackingEntity {
    @Id
    private String id;
}
