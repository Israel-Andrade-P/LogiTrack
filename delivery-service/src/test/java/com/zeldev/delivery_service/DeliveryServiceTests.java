package com.zeldev.delivery_service;

import com.zeldev.delivery_service.entity.CourierEntity;
import com.zeldev.delivery_service.entity.DeliveryEntity;
import com.zeldev.delivery_service.enumeration.DeliveryStatus;
import com.zeldev.delivery_service.event.OrderCreatedEvent;
import com.zeldev.delivery_service.exception.DeliveryNotFoundException;
import com.zeldev.delivery_service.repository.CourierRepository;
import com.zeldev.delivery_service.repository.DeliveryRepository;
import com.zeldev.delivery_service.response.DeliveryResponse;
import com.zeldev.delivery_service.service.DeliveryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.zeldev.delivery_service.enumeration.DeliveryStatus.ASSIGNED;
import static com.zeldev.delivery_service.enumeration.DeliveryStatus.PENDING_ASSIGNMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.DESC;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTests {
    @Mock
    DeliveryRepository deliveryRepository;
    @Mock
    CourierRepository courierRepository;
    @InjectMocks
    DeliveryService deliveryService;

    @Test
    @DisplayName("Should persist delivery then assign an available courier")
    void createDeliveryTest() {
        var event = new OrderCreatedEvent(
                "order-123",
                "customer-123",
                "Warehouse",
                "Customer address",
                "Parcel",
                new BigDecimal("2.500"),
                Instant.now()
                );

        var courier = CourierEntity.builder()
                .userId("courier-123")
                .available(true)
                .build();

        when(deliveryRepository.existsByOrderId("order-123")).thenReturn(false);
        when(courierRepository.findFirstByAvailableTrue()).thenReturn(Optional.of(courier));

        deliveryService.createDelivery(event);

        var entityCaptor = ArgumentCaptor.forClass(DeliveryEntity.class);
        verify(deliveryRepository).save(entityCaptor.capture());

        var savedInput = entityCaptor.getValue();
        assertThat(savedInput.getOrderId()).isEqualTo("order-123");
        assertThat(savedInput.getCourierId()).isEqualTo("courier-123");
        assertThat(savedInput.getPickupAddress()).isEqualTo("Warehouse");
        assertThat(savedInput.getDeliveryAddress()).isEqualTo("Customer address");
        assertThat(savedInput.getStatus()).isEqualTo(ASSIGNED);
        assertThat(savedInput.getPackageDescription()).isEqualTo("Parcel");
        assertThat(savedInput.getPackageWeight()).isEqualByComparingTo("2.500");
        assertThat(savedInput.getCreatedAt()).isNotNull();
        assertThat(savedInput.getUpdatedAt()).isNotNull();

        assertThat(courier.isAvailable()).isFalse();

    }

    @Test
    @DisplayName("Should not persist cause delivery already exists")
    void createDeliveryTest2() {
        var event = new OrderCreatedEvent(
                "order-123",
                "customer-123",
                "Warehouse",
                "Customer address",
                "Parcel",
                new BigDecimal("2.500"),
                Instant.now()
        );

        when(deliveryRepository.existsByOrderId("order-123")).thenReturn(true);

        deliveryService.createDelivery(event);

        verify(deliveryRepository).existsByOrderId("order-123");
        verify(deliveryRepository, never()).save(any(DeliveryEntity.class));
        verifyNoInteractions(courierRepository);
    }

    @Test
    @DisplayName("Should create a pending delivery when no courier is available")
    void createDeliveryWithoutAvailableCourierTest() {
        var event = new OrderCreatedEvent(
                "order-456",
                "customer-123",
                "Warehouse",
                "Customer address",
                "Parcel",
                new BigDecimal("2.500"),
                Instant.now()
        );

        when(deliveryRepository.existsByOrderId("order-456")).thenReturn(false);
        when(courierRepository.findFirstByAvailableTrue()).thenReturn(Optional.empty());

        deliveryService.createDelivery(event);

        var entityCaptor = ArgumentCaptor.forClass(DeliveryEntity.class);
        verify(deliveryRepository).save(entityCaptor.capture());
        verify(courierRepository).findFirstByAvailableTrue();

        var savedDelivery = entityCaptor.getValue();
        assertThat(savedDelivery.getOrderId()).isEqualTo("order-456");
        assertThat(savedDelivery.getCourierId()).isNull();
        assertThat(savedDelivery.getStatus()).isEqualTo(PENDING_ASSIGNMENT);
        assertThat(savedDelivery.getPickupAddress()).isEqualTo("Warehouse");
        assertThat(savedDelivery.getDeliveryAddress()).isEqualTo("Customer address");
        assertThat(savedDelivery.getCreatedAt()).isNotNull();
        assertThat(savedDelivery.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should get a delivery by it's id and courier id")
    void getDeliveryTest() {
        var delivery = DeliveryEntity.builder()
                .orderId("order-123")
                .courierId("courier-456")
                .status(ASSIGNED)
                .createdAt(Instant.parse("2026-01-01T12:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T12:00:00Z"))
                .build();

        when(deliveryRepository.findByOrderIdAndCourierId("order-123", "courier-456"))
                .thenReturn(Optional.of(delivery));

        var response = deliveryService.getCourierDelivery("order-123", "courier-456");

        assertThat(response.orderId()).isEqualTo("order-123");
        assertThat(response.status()).isEqualTo(ASSIGNED);
        assertThat(response.createdAt()).isEqualTo(delivery.getCreatedAt());

        verify(deliveryRepository).findByOrderIdAndCourierId("order-123", "courier-456");
    }

    @Test
    @DisplayName(("Throws DeliveryNotFoundException when id not found or delivery belongs to a different courier"))
    void getDeliveryThrowTest() {
        when(deliveryRepository.findByOrderIdAndCourierId("order-123", "courier-456"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryService.getCourierDelivery("order-123", "courier-456"))
                .isInstanceOf(DeliveryNotFoundException.class)
                .hasMessage("Delivery not found");
    }

    @Test
    @DisplayName("Should return a page delivery entity filtered by courier id")
    void getCourierDeliveriesTest() {
        var pageable = PageRequest.of(0, 20, Sort.by(DESC, "createdAt"));
        var delivery = deliveryWithStatus(ASSIGNED);
        var deliveryPage = new PageImpl<>(List.of(delivery), pageable, 1);

        when(deliveryRepository.findAllByCourierId("courier-123", pageable))
                .thenReturn(deliveryPage);

        var result = deliveryService.getCourierDeliveries("courier-123", pageable);

        assertThat(result.getContent())
                .extracting(DeliveryResponse::orderId)
                .containsExactly("order-123");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isZero();

        verify(deliveryRepository).findAllByCourierId("courier-123", pageable);
    }

    @Test
    @DisplayName("Should return a page delivery entity")
    void getAllDeliveriesTest() {
        var pageable = PageRequest.of(0, 20, Sort.by(DESC, "createdAt"));
        var delivery = deliveryWithStatus(ASSIGNED);
        var deliveryPage = new PageImpl<>(List.of(delivery), pageable, 1);

        when(deliveryRepository.findAll(pageable)).thenReturn(deliveryPage);

        var result = deliveryService.getAllDeliveries(pageable);

        assertThat(result.getContent())
                .extracting(DeliveryResponse::orderId)
                .containsExactly("order-123");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isZero();

        verify(deliveryRepository).findAll(pageable);
    }

    private DeliveryEntity deliveryWithStatus(DeliveryStatus current) {
        return DeliveryEntity.builder()
                .orderId("order-123")
                .courierId("courier-123")
                .status(current)
                .createdAt(Instant.parse("2026-01-01T12:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T12:00:00Z"))
                .build();
    }
}
