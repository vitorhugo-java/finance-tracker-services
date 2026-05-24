package com.transaction.transactionservice.transaction.application.port.in;

import java.util.UUID;

public interface DeleteTransactionUseCase {
    void delete(UUID userId, UUID transactionId);
}
