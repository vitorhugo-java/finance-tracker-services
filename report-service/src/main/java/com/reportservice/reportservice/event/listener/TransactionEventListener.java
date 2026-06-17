package com.reportservice.reportservice.event.listener;

import com.reportservice.reportservice.config.RabbitMqConfig;
import com.reportservice.reportservice.event.record.TransactionCreatedEvent;
import com.reportservice.reportservice.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    private final BalanceService balanceService;

    @RabbitListener(queues = RabbitMqConfig.TRANSACTION_CREATED_QUEUE)
    public void onTransactionCreated(TransactionCreatedEvent event) {
        log.info("Processing TransactionCreated: transactionId={} userId={}", event.transactionId(), event.userId());
        balanceService.applyTransaction(event);
    }
}