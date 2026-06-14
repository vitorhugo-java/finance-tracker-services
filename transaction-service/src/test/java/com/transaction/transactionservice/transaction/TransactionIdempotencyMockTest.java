package com.transaction.transactionservice.transaction;

import com.transaction.transactionservice.dto.internal.TransactionResult;
import com.transaction.transactionservice.dto.request.TransactionRequest;
import com.transaction.transactionservice.dto.response.TransactionResponse;
import com.transaction.transactionservice.entity.Transaction;
import com.transaction.transactionservice.entity.TransactionStatus;
import com.transaction.transactionservice.entity.TransactionType;
import com.transaction.transactionservice.event.TransactionEventPublisher;
import com.transaction.transactionservice.mapper.TransactionMapper;
import com.transaction.transactionservice.repository.TransactionRepository;
import com.transaction.transactionservice.service.TransactionService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionIdempotencyMockTest {
    @InjectMocks
    private TransactionService transactionService;
    @Mock
    private TransactionRepository repository;
    @Mock
    private RedisTemplate<String, TransactionResponse> redisTemplate;
    @Mock
    private ValueOperations<String, TransactionResponse> valueOperations;
    @Mock
    private TransactionMapper mapper;
    @Mock
    private TransactionEventPublisher publisher;

    private TransactionResponse transactionResponseMock;
    private UUID userIdMock;
    private BigDecimal amountMock;
    private String salaryMock;
    private String categoryMock;
    private OffsetDateTime nowMock;

    @BeforeEach
    public void setUp() {
        amountMock = BigDecimal.valueOf(5000.00);
        salaryMock = "Salary";
        userIdMock = UUID.randomUUID();
        categoryMock = "WORK";
        nowMock = OffsetDateTime.now();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        transactionResponseMock = new TransactionResponse(
                UUID.randomUUID(),
                userIdMock,
                salaryMock,
                amountMock,
                "category",
                TransactionType.INCOME,
                TransactionStatus.COMPLETED,
                nowMock,
                OffsetDateTime.now()
        );
    }

    @Test
    void mustReturnCachedResponse_whenKeyAlreadyExists() {
        when(redisTemplate.opsForValue().get("idempotency:transaction:key-123")).thenReturn(transactionResponseMock);

        TransactionResult transactionResult = transactionService.create(userIdMock, "key-123", new TransactionRequest(
                salaryMock,
                amountMock,
                categoryMock,
                TransactionType.INCOME,
                nowMock
        ));

        // Assert that the result is the cached response
        Assertions.assertThat(transactionResult.transactionResponse()).isEqualTo(transactionResponseMock);
        verify(repository, never()).save(any());
    }

    @Test
    void mustSaveToRepositoryAndCache_whenKeyDoesNotExist() {
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);

        Transaction transactionMock = new Transaction();
        transactionMock.setId(UUID.randomUUID());
        transactionMock.setDescription(salaryMock);
        transactionMock.setAmount(amountMock);
        transactionMock.setCategory(categoryMock);
        transactionMock.setType(TransactionType.INCOME);
        transactionMock.setTransactionDate(nowMock);

        when(mapper.toEntity(any(TransactionRequest.class))).thenReturn(transactionMock);
        when(repository.save(any(Transaction.class))).thenReturn(transactionMock);
        when(mapper.toResponse(any(Transaction.class))).thenReturn(transactionResponseMock);

        TransactionResult transactionResult = transactionService.create(userIdMock, "key-456", new TransactionRequest(
                salaryMock,
                amountMock,
                categoryMock,
                TransactionType.INCOME,
                nowMock
        ));

        // Assert that the result is the expected response
        Assertions.assertThat(transactionResult.transactionResponse()).isEqualTo(transactionResponseMock);
        verify(repository, times(1)).save(any(Transaction.class));
        verify(valueOperations, times(1)).set("idempotency:transaction:key-456", transactionResponseMock);
    }
}
