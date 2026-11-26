package com.zeldev.order_service;

import com.zeldev.order_service.entity.OrderEntity;
import com.zeldev.order_service.enumeration.OrderStatus;
import com.zeldev.order_service.event.OrderCreatedEvent;
import com.zeldev.order_service.exception.InvalidOrderStatusException;
import com.zeldev.order_service.exception.OrderNotFoundException;
import com.zeldev.order_service.publisher.OrderEventPublisher;
import com.zeldev.order_service.repository.OrderRepository;
import com.zeldev.order_service.request.OrderRequest;
import com.zeldev.order_service.response.OrderResponse;
import com.zeldev.order_service.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static com.zeldev.order_service.enumeration.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.DESC;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTests {
    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderEventPublisher publisher;
    @InjectMocks
    OrderService orderService;

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(CREATED, COLLECTED),
                Arguments.of(COLLECTED, IN_TRANSIT),
                Arguments.of(IN_TRANSIT, DELIVERED)
        );
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(CREATED, IN_TRANSIT),
                Arguments.of(CREATED, DELIVERED),
                Arguments.of(COLLECTED, CREATED),
                Arguments.of(DELIVERED, IN_TRANSIT),
                Arguments.of(CANCELLED, COLLECTED)
        );
    }

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
        inOrder.verify(publisher).publishCreatedEvent(eventCaptor.capture());

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
    void getOrderThrowTest() {
        when(orderRepository.findByPublicIdAndCustomerId("order-123", "user-456"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder("order-123", "user-456"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    @DisplayName(("Should update order status from created to cancelled"))
    void cancelOrderTest() {
        var order = OrderEntity.builder()
                .publicId("order-123")
                .customerId("user-456")
                .status(CREATED)
                .createdAt(Instant.parse("2026-01-01T12:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T12:00:00Z"))
                .build();

        when(orderRepository.findByPublicIdAndCustomerId("order-123", "user-456"))
                .thenReturn(Optional.of(order));

        var previousUpdatedAt = order.getUpdatedAt();

        var response = orderService.cancelOrder("order-123", "user-456");

        assertThat(order.getStatus()).isEqualTo(CANCELLED);

        assertThat(response.orderId()).isEqualTo("order-123");
        assertThat(response.status()).isEqualTo(CANCELLED);
        assertThat(response.updatedAt()).isAfter(previousUpdatedAt);

        verify(orderRepository).findByPublicIdAndCustomerId("order-123", "user-456");
    }

    @Test
    @DisplayName("Throws InvalidOrderStatusException when order status different from created")
    void cancelOrderThrowTest() {
        var order = OrderEntity.builder()
                .publicId("order-123")
                .customerId("user-456")
                .status(DELIVERED)
                .createdAt(Instant.parse("2026-01-01T12:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T12:00:00Z"))
                .build();

        var updatedAt = order.getUpdatedAt();

        when(orderRepository.findByPublicIdAndCustomerId("order-123", "user-456"))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder("order-123", "user-456"))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessage("Only created orders can be cancelled");

        assertThat(order.getStatus()).isEqualTo(DELIVERED);
        assertThat(order.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName(("Throws OrderNotFoundException when id not found or order belongs to a different customer"))
    void cancelOrderNotFoundTest() {
        when(orderRepository.findByPublicIdAndCustomerId("order-123", "user-456"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder("order-123", "user-456"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found");
    }

    @ParameterizedTest
    @MethodSource("validTransitions")
    @DisplayName(("Should transition an order status to the next acceptable status"))
    void transitionOrderTest(OrderStatus current, OrderStatus next) {
        var order = orderWithStatus(current);
        when(orderRepository.findByPublicId("order-123")).thenReturn(Optional.of(order));
        var oldUpdatedAt = order.getUpdatedAt();

        var response = orderService.transitionOrder("order-123", next);

        assertThat(order.getStatus()).isEqualTo(next);
        assertThat(response.status()).isEqualTo(next);
        assertThat(response.updatedAt()).isAfter(oldUpdatedAt);

        verify(orderRepository).findByPublicId("order-123");
    }

    @ParameterizedTest
    @MethodSource("invalidTransitions")
    @DisplayName("Throws InvalidOrderStatusException when it's not a valid transition")
    void transitionOrderThrowTest(OrderStatus current, OrderStatus next) {
        var order = orderWithStatus(current);
        var oldUpdatedAt = order.getUpdatedAt();
        when(orderRepository.findByPublicId("order-123")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.transitionOrder("order-123", next))
                .isInstanceOf(InvalidOrderStatusException.class);

        assertThat(order.getStatus()).isEqualTo(current);
        assertThat(order.getUpdatedAt()).isEqualTo(oldUpdatedAt);
    }

    @Test
    @DisplayName(("Throws OrderNotFoundException when id not found"))
    void transitionOrderNotFoundTest() {
        when(orderRepository.findByPublicId("order-123"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.transitionOrder("order-123", CREATED))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found");
    }


    @Test
    @DisplayName("Should return a page order entity filtered by customer id")
    void getOrdersByCustomerTest() {
        var pageable = PageRequest.of(0, 20, Sort.by(DESC, "createdAt"));
        var order = orderWithStatus(CREATED);
        var orderPage = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findAllByCustomerId("user-456", pageable))
                .thenReturn(orderPage);

        var result = orderService.getOrdersByCustomer("user-456", pageable);

        assertThat(result.getContent())
                .extracting(OrderResponse::orderId)
                .containsExactly("order-123");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isZero();

        verify(orderRepository).findAllByCustomerId("user-456", pageable);
    }

    @Test
    @DisplayName("Should return a page order entity")
    void getOrdersTest() {
        var pageable = PageRequest.of(0, 20, Sort.by(DESC, "createdAt"));
        var order = orderWithStatus(CREATED);
        var orderPage = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        var result = orderService.getOrders(pageable);

        assertThat(result.getContent())
                .extracting(OrderResponse::orderId)
                .containsExactly("order-123");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isZero();

        verify(orderRepository).findAll(pageable);
    }

    private OrderEntity orderWithStatus(OrderStatus current) {
        return OrderEntity.builder()
                .publicId("order-123")
                .customerId("user-456")
                .status(current)
                .createdAt(Instant.parse("2026-01-01T12:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T12:00:00Z"))
                .build();
    }
}
