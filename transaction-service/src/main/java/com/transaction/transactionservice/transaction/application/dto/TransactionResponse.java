package com.transaction.transactionservice.transaction.application.dto;

import com.transaction.transactionservice.transaction.domain.model.TransactionStatus;
import com.transaction.transactionservice.transaction.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Transaction response")
public record TransactionResponse(
        @Schema(example = "a3bcb4fd-9a70-4b38-93f2-e3f2bfd624c1")
        UUID id,
        @Schema(example = "151c7552-f038-41cc-a10c-e14ae2ef348f")
        UUID userId,
        @Schema(example = "Salary payment")
        String description,
        @Schema(example = "2500.00")
        BigDecimal amount,
        @Schema(example = "INCOME")
        TransactionType type,
        @Schema(example = "Salary")
        String category,
        @Schema(example = "COMPLETED")
        TransactionStatus status,
        @Schema(example = "2026-05-24")
        LocalDate transactionDate,
        Instant createdAt,
        Instant updatedAt
) {
}
