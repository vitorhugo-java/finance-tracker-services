package com.transaction.transactionservice.event;

import com.transaction.transactionservice.entity.Transaction;

public interface TransactionEventPublisher {
    void publishCreated(Transaction transaction);
}
