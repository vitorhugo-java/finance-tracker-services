package com.transaction.transactionservice.controller;

import com.transaction.transactionservice.dto.request.CreateTransactionRequest;
import com.transaction.transactionservice.dto.response.TransactionResponse;
import com.transaction.transactionservice.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a transaction", description = "Creates a financial transaction for the authenticated user")
    @ApiResponse(responseCode = "201", description = "Transaction created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public TransactionResponse create(
            @Parameter(description = "User id injected by gateway", hidden = true)
            @RequestHeader("X-User-Id")
            UUID userId,

            @Valid
            @RequestBody
            CreateTransactionRequest request
    ) {

        return transactionService.create(userId, request);
    }
}