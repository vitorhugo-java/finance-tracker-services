package com.transaction.transactionservice.transaction.infrastructure.messaging;

import com.transaction.transactionservice.config.RabbitMqConfiguration;
import com.transaction.transactionservice.transaction.application.port.out.TransactionEventPublisher;
import com.transaction.transactionservice.transaction.domain.model.Transaction;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RabbitTransactionEventPublisher implements TransactionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitTransactionEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishCreated(Transaction transaction) {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                Instant.now()
        );
        rabbitTemplate.convertAndSend(
                RabbitMqConfiguration.TRANSACTION_EVENTS_EXCHANGE,
                RabbitMqConfiguration.TRANSACTION_CREATED_ROUTING_KEY,
                event
        );
    }
}
