package com.zeldev.order_service.service;

import com.zeldev.order_service.entity.OrderEntity;
import com.zeldev.order_service.exception.OrderNotFoundException;
import com.zeldev.order_service.publisher.OrderEventPublisher;
import com.zeldev.order_service.repository.OrderRepository;
import com.zeldev.order_service.request.OrderRequest;
import com.zeldev.order_service.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

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

    private OrderEntity getOrderByIdAndCustomer(String orderId, String customerId) {
        return orderRepository.findByPublicIdAndCustomerId(orderId, customerId).orElseThrow(() -> new OrderNotFoundException("Order not found"));
    }
}
