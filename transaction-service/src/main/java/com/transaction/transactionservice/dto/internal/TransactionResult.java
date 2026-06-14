package com.transaction.transactionservice.dto.internal;


import com.transaction.transactionservice.dto.response.TransactionResponse;

public record TransactionResult(
        TransactionResponse transactionResponse,
        boolean cached
) {}
