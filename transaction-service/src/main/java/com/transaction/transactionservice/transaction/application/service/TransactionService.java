package com.transaction.transactionservice.transaction.application.service;

import com.transaction.transactionservice.transaction.application.command.CreateTransactionCommand;
import com.transaction.transactionservice.transaction.application.command.TransactionFilter;
import com.transaction.transactionservice.transaction.application.command.UpdateTransactionCommand;
import com.transaction.transactionservice.transaction.application.dto.PageResponse;
import com.transaction.transactionservice.transaction.application.dto.TransactionResponse;
import com.transaction.transactionservice.transaction.application.mapper.TransactionApplicationMapper;
import com.transaction.transactionservice.transaction.application.port.in.CreateTransactionUseCase;
import com.transaction.transactionservice.transaction.application.port.in.DeleteTransactionUseCase;
import com.transaction.transactionservice.transaction.application.port.in.GetTransactionUseCase;
import com.transaction.transactionservice.transaction.application.port.in.ListTransactionsUseCase;
import com.transaction.transactionservice.transaction.application.port.in.UpdateTransactionUseCase;
import com.transaction.transactionservice.transaction.application.port.out.IdempotencyPort;
import com.transaction.transactionservice.transaction.application.port.out.TransactionEventPublisher;
import com.transaction.transactionservice.transaction.application.port.out.TransactionRepositoryPort;
import com.transaction.transactionservice.transaction.domain.exception.BusinessException;
import com.transaction.transactionservice.transaction.domain.exception.DuplicateRequestException;
import com.transaction.transactionservice.transaction.domain.exception.ResourceNotFoundException;
import com.transaction.transactionservice.transaction.domain.exception.ValidationException;
import com.transaction.transactionservice.transaction.domain.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService implements CreateTransactionUseCase, GetTransactionUseCase, ListTransactionsUseCase,
        UpdateTransactionUseCase, DeleteTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final String TRANSACTION_ID = "transactionId";

    private final TransactionRepositoryPort repository;
    private final TransactionEventPublisher eventPublisher;
    private final IdempotencyPort idempotencyPort;
    private final TransactionApplicationMapper mapper;

    public TransactionService(TransactionRepositoryPort repository, TransactionEventPublisher eventPublisher,
                              IdempotencyPort idempotencyPort, TransactionApplicationMapper mapper) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.idempotencyPort = idempotencyPort;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TransactionResponse create(CreateTransactionCommand command) {
        validateCreate(command);
        return idempotencyPort.find(command.userId(), command.idempotencyKey())
                .orElseGet(() -> createWithIdempotencyLock(command));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getById(UUID userId, UUID transactionId) {
        return mapper.toResponse(findOwned(userId, transactionId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> list(TransactionFilter filter, Pageable pageable) {
        Page<TransactionResponse> page = repository.findAll(filter, pageable).map(mapper::toResponse);
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional
    public TransactionResponse update(UpdateTransactionCommand command) {
        validateUpdate(command);
        Transaction transaction = findOwned(command.userId(), command.transactionId());
        transaction.update(command.description(), command.amount(), command.type(), command.category(), command.transactionDate());
        Transaction saved = repository.save(transaction);
        MDC.put(TRANSACTION_ID, saved.getId().toString());
        log.info("transaction updated");
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID transactionId) {
        Transaction transaction = findOwned(userId, transactionId);
        repository.delete(transaction);
        MDC.put(TRANSACTION_ID, transaction.getId().toString());
        log.info("transaction deleted");
    }

    private TransactionResponse createNewTransaction(CreateTransactionCommand command) {
        Transaction transaction = Transaction.createPending(command.userId(), command.description(), command.amount(),
                command.type(), command.category(), command.transactionDate());
        Transaction saved = repository.save(transaction);
        MDC.put(TRANSACTION_ID, saved.getId().toString());
        try {
            eventPublisher.publishCreated(saved);
            saved.complete();
            Transaction completed = repository.save(saved);
            TransactionResponse response = mapper.toResponse(completed);
            idempotencyPort.save(command.userId(), command.idempotencyKey(), response);
            log.info("transaction created");
            return response;
        } catch (RuntimeException ex) {
            saved.fail();
            repository.save(saved);
            throw new BusinessException("Unable to publish transaction created event");
        }
    }

    private TransactionResponse createWithIdempotencyLock(CreateTransactionCommand command) {
        if (!idempotencyPort.markProcessing(command.userId(), command.idempotencyKey())) {
            return idempotencyPort.find(command.userId(), command.idempotencyKey())
                    .orElseThrow(() -> new DuplicateRequestException("Request is already being processed"));
        }
        try {
            return createNewTransaction(command);
        } catch (RuntimeException ex) {
            idempotencyPort.release(command.userId(), command.idempotencyKey());
            throw ex;
        }
    }

    private Transaction findOwned(UUID userId, UUID transactionId) {
        return repository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    private void validateCreate(CreateTransactionCommand command) {
        if (command.userId() == null) {
            throw new ValidationException("X-User-Id header is required");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new ValidationException("Idempotency-Key header is required");
        }
        validateFields(command.description(), command.amount(), command.category());
    }

    private void validateUpdate(UpdateTransactionCommand command) {
        if (command.userId() == null) {
            throw new ValidationException("X-User-Id header is required");
        }
        validateFields(command.description(), command.amount(), command.category());
    }

    private void validateFields(String description, BigDecimal amount, String category) {
        if (description == null || description.isBlank()) {
            throw new ValidationException("description must not be blank");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new ValidationException("amount must be greater than 0");
        }
        if (category == null || category.isBlank()) {
            throw new ValidationException("category is required");
        }
    }
}
