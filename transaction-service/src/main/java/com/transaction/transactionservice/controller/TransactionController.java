package com.transaction.transactionservice.controller;

import com.transaction.transactionservice.dto.request.CreateTransactionRequest;
import com.transaction.transactionservice.dto.response.TransactionPageResponse;
import com.transaction.transactionservice.dto.response.TransactionResponse;
import com.transaction.transactionservice.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create a transaction", description = "Creates a financial transaction for the authenticated user")
    @ApiResponse(responseCode = "201", description = "Transaction created successfully")
    @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            useReturnTypeSchema = true
    )
    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            useReturnTypeSchema = true
    )
    @ApiResponse(
            responseCode = "201",
            description = "New transaction created"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Transaction already exists"
    )
    public ResponseEntity<TransactionResponse> create(
            @Parameter(description = "User id injected by gateway", hidden = true)
            @RequestHeader("X-User-Id")
            UUID userId,

            @RequestHeader
            @Parameter(description = "Idempotency key for ensuring idempotent requests", example = "123e4567-e89b-12d3-a456-426614174000")
            String idempotencyKey,

            @Valid
            @RequestBody
            CreateTransactionRequest request
    ) {
        TransactionResponse response = transactionService.create(userId, idempotencyKey, request);

        if (response != null && Boolean.TRUE.equals(response.cached())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
    }

    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieves a list of all transactions for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            useReturnTypeSchema = true
    )
    public ResponseEntity<TransactionPageResponse> transactions (
            @Parameter(description = "User id injected by gateway", hidden = true)
            @RequestHeader("X-User-Id")
            UUID userId,

            @PageableDefault(size = 20)
            Pageable pageable
    ) {
        return ResponseEntity.ok(transactionService.getAllByUserId(userId, pageable));
    }
}