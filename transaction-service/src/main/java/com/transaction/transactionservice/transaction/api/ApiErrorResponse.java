package com.transaction.transactionservice.transaction.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Error response")
public record ApiErrorResponse(
        @Schema(example = "2026-05-24T10:15:30Z")
        Instant timestamp,
        @Schema(example = "400")
        int status,
        @Schema(example = "Validation failed")
        String error,
        @Schema(example = "/transactions")
        String path,
        List<FieldErrorResponse> fields
) {
}
