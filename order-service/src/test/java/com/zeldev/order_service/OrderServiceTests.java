package com.zeldev.order_service;

import com.zeldev.order_service.entity.OrderEntity;
import com.zeldev.order_service.event.OrderCreatedEvent;
import com.zeldev.order_service.exception.OrderNotFoundException;
import com.zeldev.order_service.publisher.OrderEventPublisher;
import com.zeldev.order_service.repository.OrderRepository;
import com.zeldev.order_service.request.OrderRequest;
import com.zeldev.order_service.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static com.zeldev.order_service.enumeration.OrderStatus.CREATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTests {
    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderEventPublisher publisher;
    @InjectMocks
    OrderService orderService;

    @Test
    @DisplayName("Should persist order, publish event, then return a order response")
    void placeOrderTest() {
        var request = new OrderRequest(
                "Warehouse",
                "Customer address",
                "Parcel",
                new BigDecimal("2.500")
        );
        var savedOrder = OrderEntity.builder()
                .publicId("order-123")
                .customerId("user-456")
                .pickupAddress("Warehouse")
                .deliveryAddress("Customer address")
                .packageDescription("Parcel")
                .packageWeight(new BigDecimal("2.500"))
                .status(CREATED)
                .createdAt(Instant.parse("2026-01-01T12:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T12:00:00Z"))
                .build();

        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);

        var response = orderService.place(request, "user-456");

        var entityCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        var eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        var inOrder = inOrder(orderRepository, publisher);

        inOrder.verify(orderRepository).save(entityCaptor.capture());
        inOrder.verify(publisher).publish(eventCaptor.capture());

        var savedInput = entityCaptor.getValue();
        assertThat(savedInput.getCustomerId()).isEqualTo("user-456");
        assertThat(savedInput.getPickupAddress()).isEqualTo("Warehouse");
        assertThat(savedInput.getDeliveryAddress()).isEqualTo("Customer address");
        assertThat(savedInput.getStatus()).isEqualTo(CREATED);
        assertThat(savedInput.getPackageDescription()).isEqualTo("Parcel");
        assertThat(savedInput.getPackageWeight()).isEqualByComparingTo("2.500");
        assertThat(savedInput.getCreatedAt()).isNotNull();
        assertThat(savedInput.getUpdatedAt()).isNotNull();

        var event = eventCaptor.getValue();
        assertThat(event.orderId()).isEqualTo("order-123");
        assertThat(event.customerId()).isEqualTo("user-456");
        assertThat(event.pickupAddress()).isEqualTo("Warehouse");
        assertThat(event.deliveryAddress()).isEqualTo("Customer address");
        assertThat(event.packageDescription()).isEqualTo("Parcel");
        assertThat(event.packageWeight()).isEqualByComparingTo("2.500");
        assertThat(event.createdAt()).isEqualTo(savedOrder.getCreatedAt());

        assertThat(response.orderId()).isEqualTo("order-123");
        assertThat(response.status()).isEqualTo(CREATED);
        assertThat(response.createdAt()).isEqualTo(savedOrder.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(savedOrder.getUpdatedAt());
    }

    @Test
    @DisplayName("Should get an order by it's id and customer id")
    void getOrderTest() {
        var order = OrderEntity.builder()
                .publicId("order-123")
                .customerId("user-456")
                .status(CREATED)
                .createdAt(Instant.parse("2026-01-01T12:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T12:00:00Z"))
                .build();

        when(orderRepository.findByPublicIdAndCustomerId("order-123", "user-456"))
                .thenReturn(Optional.of(order));

        var response = orderService.getOrder("order-123", "user-456");

        assertThat(response.orderId()).isEqualTo("order-123");
        assertThat(response.status()).isEqualTo(CREATED);
        assertThat(response.createdAt()).isEqualTo(order.getCreatedAt());

        verify(orderRepository).findByPublicIdAndCustomerId("order-123", "user-456");
    }

    @Test
    @DisplayName(("Throws OrderNotFoundException when id not found or order belongs to a different customer"))
    void getOrderThrow() {
        when(orderRepository.findByPublicIdAndCustomerId("order-123", "user-456"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder("order-123", "user-456"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found");
    }
}
