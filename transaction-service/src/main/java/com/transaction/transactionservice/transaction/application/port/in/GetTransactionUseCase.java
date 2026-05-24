package com.transaction.transactionservice.transaction.application.port.in;

import com.transaction.transactionservice.transaction.application.dto.TransactionResponse;

import java.util.UUID;

public interface GetTransactionUseCase {
    TransactionResponse getById(UUID userId, UUID transactionId);
}
