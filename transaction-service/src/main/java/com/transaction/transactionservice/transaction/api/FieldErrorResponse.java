package com.transaction.transactionservice.transaction.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation field error")
public record FieldErrorResponse(
        @Schema(example = "amount")
        String field,
        @Schema(example = "amount must be greater than 0")
        String message
) {
}
