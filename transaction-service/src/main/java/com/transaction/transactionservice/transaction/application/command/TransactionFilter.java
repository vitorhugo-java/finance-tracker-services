package com.transaction.transactionservice.transaction.application.command;

import com.transaction.transactionservice.transaction.domain.model.TransactionType;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionFilter(
        UUID userId,
        LocalDate from,
        LocalDate to,
        String category,
        TransactionType type,
        String search
) {
}
