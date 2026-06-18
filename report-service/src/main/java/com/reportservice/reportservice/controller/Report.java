package com.reportservice.reportservice.controller;

import com.reportservice.reportservice.dto.AccountBalanceResponse;
import com.reportservice.reportservice.service.BalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report management endpoints")
public class Report {
    private final BalanceService reportService;

    @GetMapping
    @Operation(summary = "Get user report", description = "Retrieves a financial report for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Report retrieved successfully")
    public ResponseEntity<AccountBalanceResponse> getReport(
            @Parameter(description = "User id injected by gateway", hidden = true)
            @RequestHeader("X-User-Id")
            UUID userId
    ) {
        return ResponseEntity.ok(reportService.getReport(userId));
    }
}
