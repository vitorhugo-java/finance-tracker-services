package com.transaction.transactionservice.repository;

import com.transaction.transactionservice.entity.Transaction;
import com.transaction.transactionservice.entity.TransactionType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByUserId(
            UUID userId,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndType(
            UUID userId,
            TransactionType type,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndCategoryIgnoreCase(
            UUID userId,
            String category,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndTransactionDateBetween(
            UUID userId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Pageable pageable
    );
}