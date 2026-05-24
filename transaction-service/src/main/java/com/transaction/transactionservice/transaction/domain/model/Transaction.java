package com.transaction.transactionservice.transaction.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Transaction {

    private UUID id;
    private UUID userId;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private TransactionStatus status;
    private LocalDate transactionDate;
    private Instant createdAt;
    private Instant updatedAt;

    public Transaction(UUID id, UUID userId, String description, BigDecimal amount, TransactionType type, String category,
                       TransactionStatus status, LocalDate transactionDate, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.status = status;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Transaction createPending(UUID userId, String description, BigDecimal amount, TransactionType type,
                                            String category, LocalDate transactionDate) {
        Instant now = Instant.now();
        return new Transaction(UUID.randomUUID(), userId, description, amount, type, category, TransactionStatus.PENDING,
                transactionDate, now, now);
    }

    public void update(String description, BigDecimal amount, TransactionType type, String category, LocalDate transactionDate) {
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.transactionDate = transactionDate;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        this.status = TransactionStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void fail() {
        this.status = TransactionStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public String getCategory() { return category; }
    public TransactionStatus getStatus() { return status; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
