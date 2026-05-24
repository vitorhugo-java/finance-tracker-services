package com.transaction.transactionservice.transaction.application.dto;

import com.transaction.transactionservice.transaction.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Transaction input")
public record TransactionRequest(
        @Schema(example = "Salary payment")
        @NotBlank(message = "description must not be blank")
        String description,

        @Schema(example = "2500.00")
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "amount must be greater than 0")
        BigDecimal amount,

        @Schema(example = "INCOME")
        @NotNull(message = "type is required")
        TransactionType type,

        @Schema(example = "Salary")
        @NotBlank(message = "category is required")
        String category,

        @Schema(example = "2026-05-24")
        @NotNull(message = "transactionDate is required")
        LocalDate transactionDate
) {
}
