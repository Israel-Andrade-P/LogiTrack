package com.zeldev.order_service.service;

import com.zeldev.order_service.entity.OrderEntity;
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

    private OrderEntity getOrderByIdAndCustomer(String orderId, String customerId) {
        return orderRepository.findByPublicIdAndCustomerId(orderId, customerId).orElseThrow(() -> new OrderNotFoundException("Order not found"));
    }

    public OrderResponse deleteOrder(String orderId, String subject) {
        return null;
    }
}
