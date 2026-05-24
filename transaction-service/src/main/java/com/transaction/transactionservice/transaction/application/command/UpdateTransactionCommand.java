package com.transaction.transactionservice.transaction.application.command;

import com.transaction.transactionservice.transaction.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTransactionCommand(
        UUID userId,
        UUID transactionId,
        String description,
        BigDecimal amount,
        TransactionType type,
        String category,
        LocalDate transactionDate
) {
}
