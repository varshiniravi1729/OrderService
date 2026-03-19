package com.example.order_service.producer;

import com.example.order_service.config.RabbitMQConfig;
import com.example.order_service.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// Dedicated producer class — responsible ONLY for publishing messages to RabbitMQ
// Keeping this separate from OrderService makes the code easier to understand:
//   OrderService  → handles business logic (save order, build event)
//   OrderProducer → handles messaging logic (which exchange, which routing key)
@Component
@RequiredArgsConstructor
public class OrderProducer {

    // RabbitTemplate is the core Spring AMQP class used to send messages to RabbitMQ
    private final RabbitTemplate rabbitTemplate;

    // Publishes to DIRECT EXCHANGE with exact routing key "order.created"
    // Only payment.queue receives this — exact key match required
    public void sendToDirectExchange(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DIRECT_EXCHANGE,       // target exchange
                RabbitMQConfig.DIRECT_ROUTING_KEY,    // exact routing key: "order.created"
                event                                 // message payload
        );
    }

    // Publishes to TOPIC EXCHANGE with a dynamic region-based routing key
    // e.g. region="india" → routingKey="order.india" → order.india.queue receives it
    //      region="usa"   → routingKey="order.usa"   → order.usa.queue receives it
    public void sendToTopicExchange(OrderCreatedEvent event, String region) {
        String routingKey = "order." + region;  // dynamically built: "order.india" or "order.usa"
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TOPIC_EXCHANGE,        // target exchange
                routingKey,                           // pattern-based routing key
                event                                 // message payload
        );
    }
}
