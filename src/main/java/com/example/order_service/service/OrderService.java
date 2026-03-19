package com.example.order_service.service;

import com.example.order_service.dto.OrderCreatedEvent;
import com.example.order_service.dto.OrderRequestDTO;
import com.example.order_service.dto.OrderResponseDTO;
import com.example.order_service.entity.Order;
import com.example.order_service.producer.OrderProducer;
import com.example.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;

    // OrderProducer handles all RabbitMQ publishing — keeps messaging logic out of here
    private final OrderProducer orderProducer;

    public OrderResponseDTO createOrder(OrderRequestDTO dto) {

        // Step 1: Save order to DB
        Order order = new Order();
        order.setProductName(dto.getProductName());
        order.setQuantity(dto.getQuantity());
        order.setPrice(dto.getPrice());
        order.setStatus("CREATED");
        Order saved = repository.save(order);

        // Step 2: Build the event payload to send via RabbitMQ
        OrderCreatedEvent event = new OrderCreatedEvent(saved.getId(), saved.getPrice());

        // Step 3: Publish to Direct Exchange — exact routing to payment.queue
        orderProducer.sendToDirectExchange(event);

//        // Step 4: Publish to Topic Exchang e — region-based routing
//        // "india" → order.india.queue, "usa" → order.usa.queue
        orderProducer.sendToTopicExchange(event, dto.getRegion());

        return new OrderResponseDTO(saved.getId(), "ORDER_CREATED");
    }
}
