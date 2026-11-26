package com.zeldev.delivery_service.service;

import com.zeldev.delivery_service.entity.CourierEntity;
import com.zeldev.delivery_service.event.OrderCreatedEvent;
import com.zeldev.delivery_service.event.OrderStatusUpdateEvent;
import com.zeldev.delivery_service.enumeration.OrderStatus;
import com.zeldev.delivery_service.exception.DeliveryNotFoundException;
import com.zeldev.delivery_service.repository.CourierRepository;
import com.zeldev.delivery_service.repository.DeliveryRepository;
import com.zeldev.delivery_service.response.DeliveryResponse;
import com.zeldev.delivery_service.utils.DeliveryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.zeldev.delivery_service.enumeration.DeliveryStatus.ASSIGNED;
import static com.zeldev.delivery_service.enumeration.DeliveryStatus.CANCELLED;
import static com.zeldev.delivery_service.enumeration.DeliveryStatus.PENDING_ASSIGNMENT;
import static com.zeldev.delivery_service.utils.DeliveryMapper.toEntity;
import static com.zeldev.delivery_service.utils.DeliveryMapper.toResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final CourierRepository courierRepository;

    @Transactional
    public void createDelivery(OrderCreatedEvent event) {
        if (checkByOrderId(event.orderId())) {
            log.info("Delivery with order ID {} already exists", event.orderId());
            return;
        }

        var deliveryStatus = ASSIGNED;

        var optionalCourier = findAvailableCourier();

        if (optionalCourier.isEmpty()) {
            deliveryStatus = PENDING_ASSIGNMENT;
            deliveryRepository.save(toEntity(event, null, deliveryStatus));
            return;
        }

        var courier = optionalCourier.get();

        deliveryRepository.save(toEntity(event, courier.getUserId(), deliveryStatus));

        courier.setAvailable(false);
    }

    @Transactional
    public void cancelDelivery(OrderStatusUpdateEvent event) {
        if (event.newStatus() != OrderStatus.CANCELLED) return;

        var delivery = deliveryRepository.findByOrderId(event.orderId())
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found for order " + event.orderId()));

        if (delivery.getStatus() == CANCELLED) return;

        delivery.setStatus(CANCELLED);
        delivery.setUpdatedAt(event.occurredAt());

        if (delivery.getCourierId() != null) {
            courierRepository.findById(delivery.getCourierId())
                    .ifPresent(courier -> courier.setAvailable(true));
        }
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getCourierDelivery(String orderId, String courierId) {
        var delivery = deliveryRepository.findByOrderIdAndCourierId(orderId, courierId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found"));
        return toResponse(delivery);
    }

    @Transactional(readOnly = true)
    public Page<DeliveryResponse> getCourierDeliveries(String courierId, Pageable pageable) {
        return deliveryRepository.findAllByCourierId(courierId, pageable).map(DeliveryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<DeliveryResponse> getAllDeliveries(Pageable pageable) {
        return deliveryRepository.findAll(pageable).map(DeliveryMapper::toResponse);
    }

    private boolean checkByOrderId(String orderId) {
        return deliveryRepository.existsByOrderId(orderId);
    }

    private Optional<CourierEntity> findAvailableCourier() {
        return courierRepository.findFirstByAvailableTrue();
    }
}
