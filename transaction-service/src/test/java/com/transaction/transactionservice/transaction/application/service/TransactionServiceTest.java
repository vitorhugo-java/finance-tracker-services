package com.transaction.transactionservice.transaction.application.service;

import com.transaction.transactionservice.transaction.application.command.CreateTransactionCommand;
import com.transaction.transactionservice.transaction.application.dto.TransactionResponse;
import com.transaction.transactionservice.transaction.application.mapper.TransactionApplicationMapper;
import com.transaction.transactionservice.transaction.application.port.out.IdempotencyPort;
import com.transaction.transactionservice.transaction.application.port.out.TransactionEventPublisher;
import com.transaction.transactionservice.transaction.application.port.out.TransactionRepositoryPort;
import com.transaction.transactionservice.transaction.domain.model.Transaction;
import com.transaction.transactionservice.transaction.domain.model.TransactionStatus;
import com.transaction.transactionservice.transaction.domain.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionServiceTest {

    private final TransactionRepositoryPort repository = mock(TransactionRepositoryPort.class);
    private final TransactionEventPublisher publisher = mock(TransactionEventPublisher.class);
    private final IdempotencyPort idempotency = mock(IdempotencyPort.class);
    private final TransactionApplicationMapper mapper = mock(TransactionApplicationMapper.class);
    private final TransactionService service = new TransactionService(repository, publisher, idempotency, mapper);

    @Test
    void returnsCachedResponseWhenIdempotencyKeyWasProcessed() {
        UUID userId = UUID.randomUUID();
        TransactionResponse cached = response(UUID.randomUUID(), userId);
        when(idempotency.find(userId, "key-1")).thenReturn(Optional.of(cached));

        TransactionResponse result = service.create(command(userId));

        assertThat(result).isEqualTo(cached);
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(publisher, never()).publishCreated(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createsPendingTransactionPublishesEventAndCompletesIt() {
        UUID userId = UUID.randomUUID();
        when(idempotency.find(userId, "key-1")).thenReturn(Optional.empty());
        when(idempotency.markProcessing(userId, "key-1")).thenReturn(true);
        when(repository.save(org.mockito.ArgumentMatchers.any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(org.mockito.ArgumentMatchers.any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction transaction = invocation.getArgument(0);
                    return response(transaction.getId(), transaction.getUserId());
                });
        doAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
            return null;
        }).when(publisher).publishCreated(org.mockito.ArgumentMatchers.any(Transaction.class));

        TransactionResponse result = service.create(command(userId));

        assertThat(result.userId()).isEqualTo(userId);
        verify(publisher).publishCreated(org.mockito.ArgumentMatchers.any(Transaction.class));
        verify(idempotency).save(org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.eq("key-1"),
                org.mockito.ArgumentMatchers.any(TransactionResponse.class));
    }

    private CreateTransactionCommand command(UUID userId) {
        return new CreateTransactionCommand(userId, "Salary", BigDecimal.TEN, TransactionType.INCOME, "Salary",
                LocalDate.of(2026, 5, 24), "key-1");
    }

    private TransactionResponse response(UUID transactionId, UUID userId) {
        return new TransactionResponse(transactionId, userId, "Salary", BigDecimal.TEN, TransactionType.INCOME,
                "Salary", TransactionStatus.COMPLETED, LocalDate.of(2026, 5, 24), Instant.now(), Instant.now());
    }
}
