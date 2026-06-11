package com.transaction.transactionservice.repository;

import com.transaction.transactionservice.entity.Transaction;
import com.transaction.transactionservice.entity.TransactionType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findAllByUserId(UUID userId, Pageable pageable);
}