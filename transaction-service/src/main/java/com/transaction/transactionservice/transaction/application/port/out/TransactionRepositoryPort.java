package com.transaction.transactionservice.transaction.application.port.out;

import com.transaction.transactionservice.transaction.application.command.TransactionFilter;
import com.transaction.transactionservice.transaction.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);

    Optional<Transaction> findByIdAndUserId(UUID transactionId, UUID userId);

    Page<Transaction> findAll(TransactionFilter filter, Pageable pageable);

    void delete(Transaction transaction);
}
