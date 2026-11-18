package com.zeldev.order_service.service;

import com.zeldev.order_service.entity.OrderEntity;
import com.zeldev.order_service.enumeration.OrderStatus;
import com.zeldev.order_service.exception.InvalidOrderStatusException;
import com.zeldev.order_service.exception.OrderNotFoundException;
import com.zeldev.order_service.publisher.OrderEventPublisher;
import com.zeldev.order_service.repository.OrderRepository;
import com.zeldev.order_service.request.OrderRequest;
import com.zeldev.order_service.response.OrderResponse;
import com.zeldev.order_service.utils.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.zeldev.order_service.utils.OrderEvent.createEvent;
import static com.zeldev.order_service.utils.OrderMapper.toEntity;
import static com.zeldev.order_service.utils.OrderMapper.toResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEventPublisher publisher;
    /*
    Order lifecycles:
    CREATED → COLLECTED → IN_TRANSIT → DELIVERED
    CREATED -> CANCELLED
     */

    public OrderResponse place(OrderRequest request, String userId) {
        var entity = orderRepository.save(toEntity(request, userId));
        var event = createEvent(entity);

        log.info("Publishing a event to kafka broker for order: {}", entity.getPublicId());
        publisher.publish(event);

        return toResponse(entity);
    }

    public OrderResponse getOrder(String orderId, String userId) {
        return toResponse(getOrderByIdAndCustomer(orderId, userId));
    }

    public Page<OrderResponse> getOrdersByCustomer(String customerId, Pageable pageable) {
        return orderRepository.findAllByCustomerId(customerId, pageable).map(OrderMapper::toResponse);
    }

    public Page<OrderResponse> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderMapper::toResponse);
    }

    @Transactional
    public OrderResponse cancelOrder(String orderId, String customerId) {
        var order = getOrderByIdAndCustomer(orderId, customerId);
        if (order.getStatus() != OrderStatus.CREATED) throw new InvalidOrderStatusException("Only created orders can be cancelled");

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());

        return toResponse(order);
    }

    @Transactional
    public OrderResponse transitionOrder(String orderId, OrderStatus nextStatus) {
        var order = orderRepository.findByPublicId(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (!isValidTransition(order.getStatus(), nextStatus)) {
            throw new InvalidOrderStatusException(
                    "Cannot transition from " + order.getStatus() + " to " + nextStatus
            );
        }

        order.setStatus(nextStatus);
        order.setUpdatedAt(Instant.now());

        return toResponse(order);
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case CREATED -> next == OrderStatus.COLLECTED;
            case COLLECTED -> next == OrderStatus.IN_TRANSIT;
            case IN_TRANSIT -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    private OrderEntity getOrderByIdAndCustomer(String orderId, String customerId) {
        return orderRepository.findByPublicIdAndCustomerId(orderId, customerId).orElseThrow(() -> new OrderNotFoundException("Order not found"));
    }
}
