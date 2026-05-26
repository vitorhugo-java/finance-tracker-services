package com.transaction.transactionservice.service;

import com.transaction.transactionservice.dto.request.CreateTransactionRequest;
import com.transaction.transactionservice.dto.response.TransactionResponse;
import com.transaction.transactionservice.entity.Transaction;
import com.transaction.transactionservice.event.TransactionEventPublisher;
import com.transaction.transactionservice.mapper.TransactionMapper;
import com.transaction.transactionservice.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final TransactionEventPublisher publisher;

    public TransactionResponse create(UUID userId, CreateTransactionRequest request) {
        Transaction transaction = mapper.toEntity(request);
        transaction.setUserId(userId);
        Transaction saved = repository.save(transaction);
        publisher.publishCreated(saved);
        return mapper.toResponse(saved);
    }
}
