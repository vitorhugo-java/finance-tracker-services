package com.transaction.transactionservice.transaction.application.port.out;

import com.transaction.transactionservice.transaction.domain.model.Transaction;

public interface TransactionEventPublisher {
    void publishCreated(Transaction transaction);
}
