package com.transaction.transactionservice.event.record;

import com.transaction.transactionservice.entity.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID transactionId,
        UUID userId,
        BigDecimal amount,
        TransactionType type,
        String category,
        OffsetDateTime occurredAt
) {}
