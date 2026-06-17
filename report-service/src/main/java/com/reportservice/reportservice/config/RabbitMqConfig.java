package com.reportservice.reportservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String TRANSACTION_EVENTS_EXCHANGE = "transaction.events";
    public static final String TRANSACTION_CREATED_ROUTING_KEY = "transaction.created";
    public static final String TRANSACTION_CREATED_QUEUE = "report.transaction-created.queue";

    @Bean
    DirectExchange transactionEventsExchange() {
        return new DirectExchange(TRANSACTION_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    Queue transactionCreatedQueue() {
        return QueueBuilder.durable(TRANSACTION_CREATED_QUEUE).build();
    }

    @Bean
    Binding transactionCreatedBinding(Queue transactionCreatedQueue, DirectExchange transactionEventsExchange) {
        return BindingBuilder.bind(transactionCreatedQueue)
                .to(transactionEventsExchange)
                .with(TRANSACTION_CREATED_ROUTING_KEY);
    }

    @Bean
    JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}