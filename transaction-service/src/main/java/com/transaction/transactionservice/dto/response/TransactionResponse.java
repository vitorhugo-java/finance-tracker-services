package com.transaction.transactionservice.dto.response;

import com.transaction.transactionservice.entity.Transaction;
import com.transaction.transactionservice.entity.TransactionStatus;
import com.transaction.transactionservice.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Transaction response")
public record TransactionResponse(

        @Schema(
                description = "Transaction id",
                example = "01970b7f-7ef5-77aa-9cc5-71df35eb5f3e"
        )
        UUID id,

        @Schema(
                description = "Authenticated user id",
                example = "01970b4f-69a7-75d5-bfa8-7f2a5514f301"
        )
        UUID userId,

        @Schema(
                description = "Transaction description",
                example = "Monthly salary"
        )
        String description,

        @Schema(
                description = "Transaction amount",
                example = "5000.00"
        )
        BigDecimal amount,

        @Schema(
                description = "Category",
                example = "WORK"
        )
        String category,

        @Schema(
                description = "Transaction type",
                allowableValues = {"INCOME", "EXPENSE"}
        )
        TransactionType type,

        @Schema(
                description = "Current transaction status",
                allowableValues = {
                        "PENDING",
                        "COMPLETED",
                        "FAILED"
                }
        )
        TransactionStatus status,

        @Schema(
                description = "Transaction date",
                example = "2026-05-24T15:30:00Z"
        )
        OffsetDateTime transactionDate,

        @Schema(
                description = "Creation timestamp",
                example = "2026-05-24T15:31:00Z"
        )
        OffsetDateTime createdAt,

        @Schema(
                description = "cached",
                example = "true"
        )
        Boolean cached
) {
}