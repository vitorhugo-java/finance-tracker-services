package com.transaction.transactionservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated transaction response")
public record TransactionPageResponse (
    @Schema(description = "Transactions on this page", implementation = TransactionResponse.class)
    List<TransactionResponse> content,
    @Schema(description = "Current page number (0-based)", example = "0")
    int pageNumber,
    @Schema(description = "Number of items per page", example = "10")
    int pageSize,
    @Schema(description = "Total number of transactions matching the filter", example = "42")
    long totalElements,
    @Schema(description = "Total number of pages", example = "5")
    int totalPages
) {}