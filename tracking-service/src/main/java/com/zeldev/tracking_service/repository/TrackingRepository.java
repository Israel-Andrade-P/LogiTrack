package com.zeldev.tracking_service.repository;

import com.zeldev.tracking_service.entity.TrackingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrackingRepository extends MongoRepository<TrackingEntity, String> {
}
