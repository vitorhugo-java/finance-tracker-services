package com.transaction.transactionservice.event.impl;

import com.transaction.transactionservice.config.RabbitMqConfiguration;
import com.transaction.transactionservice.entity.Transaction;
import com.transaction.transactionservice.event.TransactionEventPublisher;
import com.transaction.transactionservice.event.record.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitTransactionEventPublisher implements TransactionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishCreated(Transaction transaction) {

        var event = new TransactionCreatedEvent(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getTransactionDate()
        );

        rabbitTemplate.convertAndSend(
                RabbitMqConfiguration.TRANSACTION_EVENTS_EXCHANGE,
                RabbitMqConfiguration.TRANSACTION_CREATED_ROUTING_KEY,
                event
        );
    }
}