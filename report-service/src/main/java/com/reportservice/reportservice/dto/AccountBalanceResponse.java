package com.reportservice.reportservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Response payload for user report")
public record AccountBalanceResponse(
        @Schema(
                description = "User ID",
                example = "123e4567-e89b-12d3-a456-426614174000"
        )
        String userId,
        @Schema(
                description = "Current account balance",
                example = "1500.75"
        )
        BigDecimal balance,
        @Schema(
                description = "Timestamp of the last balance update",
                example = "2026-05-24T15:30:00Z"
        )
        OffsetDateTime updatedAt
) {
}
