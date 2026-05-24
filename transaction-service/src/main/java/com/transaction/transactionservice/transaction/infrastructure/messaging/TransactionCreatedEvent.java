package com.transaction.transactionservice.transaction.infrastructure.messaging;

import com.transaction.transactionservice.transaction.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID transactionId,
        UUID userId,
        BigDecimal amount,
        TransactionType type,
        String category,
        Instant occurredAt
) {
}
