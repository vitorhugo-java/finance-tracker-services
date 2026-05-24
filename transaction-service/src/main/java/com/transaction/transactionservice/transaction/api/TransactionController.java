package com.transaction.transactionservice.transaction.api;

import com.transaction.transactionservice.transaction.application.command.CreateTransactionCommand;
import com.transaction.transactionservice.transaction.application.command.TransactionFilter;
import com.transaction.transactionservice.transaction.application.command.UpdateTransactionCommand;
import com.transaction.transactionservice.transaction.application.dto.PageResponse;
import com.transaction.transactionservice.transaction.application.dto.TransactionRequest;
import com.transaction.transactionservice.transaction.application.dto.TransactionResponse;
import com.transaction.transactionservice.transaction.application.port.in.CreateTransactionUseCase;
import com.transaction.transactionservice.transaction.application.port.in.DeleteTransactionUseCase;
import com.transaction.transactionservice.transaction.application.port.in.GetTransactionUseCase;
import com.transaction.transactionservice.transaction.application.port.in.ListTransactionsUseCase;
import com.transaction.transactionservice.transaction.application.port.in.UpdateTransactionUseCase;
import com.transaction.transactionservice.transaction.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Authenticated user transaction management")
public class TransactionController {

    private final CreateTransactionUseCase createTransaction;
    private final GetTransactionUseCase getTransaction;
    private final ListTransactionsUseCase listTransactions;
    private final UpdateTransactionUseCase updateTransaction;
    private final DeleteTransactionUseCase deleteTransaction;

    public TransactionController(CreateTransactionUseCase createTransaction, GetTransactionUseCase getTransaction,
                                 ListTransactionsUseCase listTransactions, UpdateTransactionUseCase updateTransaction,
                                 DeleteTransactionUseCase deleteTransaction) {
        this.createTransaction = createTransaction;
        this.getTransaction = getTransaction;
        this.listTransactions = listTransactions;
        this.updateTransaction = updateTransaction;
        this.deleteTransaction = deleteTransaction;
    }

    @PostMapping
    @Operation(
            summary = "Create transaction",
            description = "Creates a PENDING transaction, publishes TransactionCreated, then marks it COMPLETED. Requires Gateway user headers and Idempotency-Key.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            headers = @Header(name = "Location", description = "Created transaction URI"),
                            content = @Content(schema = @Schema(implementation = TransactionResponse.class),
                                    examples = @ExampleObject(value = """
                                            {"id":"a3bcb4fd-9a70-4b38-93f2-e3f2bfd624c1","userId":"151c7552-f038-41cc-a10c-e14ae2ef348f","description":"Salary payment","amount":2500.00,"type":"INCOME","category":"Salary","status":"COMPLETED","transactionDate":"2026-05-24","createdAt":"2026-05-24T10:15:30Z","updatedAt":"2026-05-24T10:15:31Z"}
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {"timestamp":"2026-05-24T10:15:30Z","status":400,"error":"Validation failed","path":"/transactions","fields":[{"field":"amount","message":"amount must be greater than 0"}]}
                                            """))),
                    @ApiResponse(responseCode = "409", description = "Business conflict",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            }
    )
    public ResponseEntity<TransactionResponse> create(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    schema = @Schema(implementation = TransactionRequest.class),
                    examples = @ExampleObject(value = """
                            {"description":"Salary payment","amount":2500.00,"type":"INCOME","category":"Salary","transactionDate":"2026-05-24"}
                            """)))
            @Valid @RequestBody TransactionRequest request) {
        UserContext context = UserContext.fromHeaders(userId, email, roles);
        TransactionResponse response = createTransaction.create(new CreateTransactionCommand(context.userId(),
                request.description(), request.amount(), request.type(), request.category(), request.transactionDate(), idempotencyKey));
        return ResponseEntity.created(URI.create("/transactions/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by id", responses = {
            @ApiResponse(responseCode = "200", description = "Found", content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public TransactionResponse getById(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @PathVariable UUID id) {
        return getTransaction.getById(UserContext.fromHeaders(userId, email, roles).userId(), id);
    }

    @GetMapping
    @Operation(summary = "List and search transactions", description = "Supports pagination, date range, category, transaction type, and text search over history.")
    public PageResponse<TransactionResponse> list(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @Parameter(description = "Start date, inclusive", example = "2026-05-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date, inclusive", example = "2026-05-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Category", example = "Groceries")
            @RequestParam(required = false) String category,
            @Parameter(description = "Transaction type", example = "EXPENSE")
            @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Search description or category", example = "market")
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "transactionDate") Pageable pageable) {
        UserContext context = UserContext.fromHeaders(userId, email, roles);
        return listTransactions.list(new TransactionFilter(context.userId(), from, to, category, type, search), pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction", responses = {
            @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public TransactionResponse update(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @PathVariable UUID id,
            @Valid @RequestBody TransactionRequest request) {
        UserContext context = UserContext.fromHeaders(userId, email, roles);
        return updateTransaction.update(new UpdateTransactionCommand(context.userId(), id, request.description(),
                request.amount(), request.type(), request.category(), request.transactionDate()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction", responses = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @PathVariable UUID id) {
        deleteTransaction.delete(UserContext.fromHeaders(userId, email, roles).userId(), id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
