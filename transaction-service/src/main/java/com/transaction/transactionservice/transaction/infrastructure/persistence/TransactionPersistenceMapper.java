package com.transaction.transactionservice.transaction.infrastructure.persistence;

import com.transaction.transactionservice.transaction.domain.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionPersistenceMapper {

    TransactionEntity toEntity(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(transaction.getId());
        entity.setUserId(transaction.getUserId());
        entity.setDescription(transaction.getDescription());
        entity.setAmount(transaction.getAmount());
        entity.setType(transaction.getType());
        entity.setCategory(transaction.getCategory());
        entity.setStatus(transaction.getStatus());
        entity.setTransactionDate(transaction.getTransactionDate());
        entity.setCreatedAt(transaction.getCreatedAt());
        entity.setUpdatedAt(transaction.getUpdatedAt());
        return entity;
    }

    Transaction toDomain(TransactionEntity entity) {
        return new Transaction(entity.getId(), entity.getUserId(), entity.getDescription(), entity.getAmount(),
                entity.getType(), entity.getCategory(), entity.getStatus(), entity.getTransactionDate(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
