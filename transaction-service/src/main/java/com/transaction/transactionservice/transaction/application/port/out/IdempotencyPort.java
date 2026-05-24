package com.transaction.transactionservice.transaction.application.port.out;

import com.transaction.transactionservice.transaction.application.dto.TransactionResponse;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyPort {
    Optional<TransactionResponse> find(UUID userId, String idempotencyKey);

    boolean markProcessing(UUID userId, String idempotencyKey);

    void save(UUID userId, String idempotencyKey, TransactionResponse response);

    void release(UUID userId, String idempotencyKey);
}
