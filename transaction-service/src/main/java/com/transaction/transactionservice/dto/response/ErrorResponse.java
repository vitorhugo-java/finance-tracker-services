package com.transaction.transactionservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Standard API error response")
public record ErrorResponse(

        @Schema(
                description = "Timestamp when the error occurred",
                example = "2026-05-26T21:45:00Z"
        )
        OffsetDateTime timestamp,

        @Schema(
                description = "HTTP status code",
                example = "400"
        )
        Integer status,

        @Schema(
                description = "Error category",
                example = "Validation Error"
        )
        String error,

        @Schema(
                description = "Human-readable error message",
                example = "Request validation failed"
        )
        String message,

        @Schema(
                description = "Detailed validation or processing errors",
                example = """
                [
                  "description: Description is required",
                  "amount: Amount must be greater than zero"
                ]
                """
        )
        List<String> details

) {
}