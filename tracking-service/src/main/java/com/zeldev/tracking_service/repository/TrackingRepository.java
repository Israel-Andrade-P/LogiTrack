package com.zeldev.tracking_service.repository;

import com.zeldev.tracking_service.entity.TrackingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TrackingRepository extends MongoRepository<TrackingEntity, String> {

    List<TrackingEntity> findAllByOrderIdOrderByOccurredAtAsc(String orderId);
}
