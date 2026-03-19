package com.example.order_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // =============================================================
    // DIRECT EXCHANGE
    // =============================================================
    // How it works:
    //   Message is routed to the queue whose binding key EXACTLY
    //   matches the routing key sent by the producer.
    //
    // Use case: Exact routing
    //   e.g. routingKey = "order.created"  →  only payment.queue gets it
    //        routingKey = "order.cancelled" →  payment.queue does NOT get it
    //
    // Real-world analogy: A letter with a specific address —
    //   it goes to exactly that one mailbox, no one else.
    // =============================================================

    public static final String DIRECT_EXCHANGE    = "order.direct.exchange";
    public static final String PAYMENT_QUEUE      = "payment.queue";
    public static final String DIRECT_ROUTING_KEY = "order.created";

    // DirectExchange routes messages using exact routing key matching
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    // Queue that payment-service listens to for order payment processing
    // Configured with DLX arguments — if a message is rejected after retries,
    // RabbitMQ automatically forwards it to dlx.exchange → payment.dlq
    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "dlq.routing")
                .build();
    }

    // Bind payment.queue to direct exchange with exact key "order.created"
    // Only messages with routingKey="order.created" will reach this queue
    @Bean
    public Binding directBinding() {
        return BindingBuilder
                .bind(paymentQueue())
                .to(directExchange())
                .with(DIRECT_ROUTING_KEY);
    }


    // =============================================================
    // TOPIC EXCHANGE
    // =============================================================
    // How it works:
    //   Routing key supports wildcard patterns:
    //     *  matches exactly ONE word
    //     #  matches ZERO or more words
    //
    // Use case: Pattern-based routing
    //   e.g. routingKey = "order.india" → order.india.queue gets it
    //        routingKey = "order.usa"   → order.usa.queue gets it
    //        A queue bound with "order.#" would receive ALL order messages
    //        A queue bound with "*.india" would receive any service's india events
    //
    // Real-world analogy: A newspaper subscription —
    //   subscribe to "sports.*" and you get all sports sections,
    //   subscribe to "sports.cricket" and you get only cricket.
    // =============================================================

    public static final String TOPIC_EXCHANGE         = "order.topic.exchange";
    public static final String ORDER_INDIA_QUEUE      = "order.india.queue";
    public static final String ORDER_USA_QUEUE        = "order.usa.queue";
    public static final String TOPIC_ROUTING_INDIA    = "order.india";
    public static final String TOPIC_ROUTING_USA      = "order.usa";

    // TopicExchange routes messages using wildcard pattern matching on routing keys
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    // Queue for orders placed from India region
    @Bean
    public Queue orderIndiaQueue() {
        return new Queue(ORDER_INDIA_QUEUE);
    }

    // Queue for orders placed from USA region
    @Bean
    public Queue orderUsaQueue() {
        return new Queue(ORDER_USA_QUEUE);
    }

    // Bind india queue — receives only messages with routing key "order.india"
    @Bean
    public Binding topicIndiaBinding() {
        return BindingBuilder
                .bind(orderIndiaQueue())
                .to(topicExchange())
                .with(TOPIC_ROUTING_INDIA);
    }

    // Bind usa queue — receives only messages with routing key "order.usa"
    @Bean
    public Binding topicUsaBinding() {
        return BindingBuilder
                .bind(orderUsaQueue())
                .to(topicExchange())
                .with(TOPIC_ROUTING_USA);
    }


    // Converts Java objects to JSON when publishing so consumers can deserialize them
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
