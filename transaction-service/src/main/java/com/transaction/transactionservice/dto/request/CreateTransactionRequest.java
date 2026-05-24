package com.transaction.transactionservice.dto.request;

import com.transaction.transactionservice.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Payload for transaction creation")
public record CreateTransactionRequest(

        @Schema(
                description = "Transaction description",
                example = "Monthly salary"
        )
        @NotBlank(message = "Description is required")
        String description,

        @Schema(
                description = "Transaction amount",
                example = "5000.00"
        )
        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        BigDecimal amount,

        @Schema(
                description = "Transaction category",
                example = "WORK"
        )
        @NotBlank(message = "Category is required")
        String category,

        @Schema(
                description = "Transaction type",
                allowableValues = {"INCOME", "EXPENSE"},
                example = "INCOME"
        )
        @NotNull(message = "Transaction type is required")
        TransactionType type,

        @Schema(
                description = "Transaction date",
                example = "2026-05-24T15:30:00Z"
        )
        @NotNull(message = "Transaction date is required")
        OffsetDateTime transactionDate

) {
}