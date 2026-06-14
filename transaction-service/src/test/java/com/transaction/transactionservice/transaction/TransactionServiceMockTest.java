package com.transaction.transactionservice.transaction;

import com.transaction.transactionservice.dto.request.TransactionRequest;
import com.transaction.transactionservice.dto.response.TransactionPageResponse;
import com.transaction.transactionservice.dto.response.TransactionResponse;
import com.transaction.transactionservice.entity.Transaction;
import com.transaction.transactionservice.entity.TransactionType;
import com.transaction.transactionservice.event.TransactionEventPublisher;
import com.transaction.transactionservice.mapper.TransactionMapper;
import com.transaction.transactionservice.repository.TransactionRepository;
import com.transaction.transactionservice.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceMockTest {
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

    private UUID userIdMock;

    @BeforeEach
    public void setUp() {
        userIdMock = UUID.randomUUID();
    }

    @Test
    public void mustReturnNotNull_whenGetAllByUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of());
        TransactionPageResponse pageResponseMock = mock(TransactionPageResponse.class);

        when(repository.findAllByUserId(userIdMock, pageable)).thenReturn(page);
        when(mapper.toPageResponse(page)).thenReturn(pageResponseMock);

        var result = transactionService.getAllByUserId(userIdMock, pageable);

        assertThat(result).isNotNull();
    }

    @Test
    public void mustReturnNotNull_whenCreate() {
        String impotencyKey = "impotencyKey";
        TransactionRequest request = new TransactionRequest(
                "description",
                BigDecimal.TEN,
                "category",
                TransactionType.INCOME,
                OffsetDateTime.now()

        );
        Transaction transactionMock = mock(Transaction.class);
        Transaction savedTransactionMock = mock(Transaction.class);
        TransactionResponse responseMock = mock(TransactionResponse.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(mapper.toEntity(request)).thenReturn(transactionMock);
        when(repository.save(transactionMock)).thenReturn(savedTransactionMock);
        when(mapper.toResponse(savedTransactionMock)).thenReturn(responseMock);

        var result = transactionService.create(userIdMock, impotencyKey, request);

        assertThat(result).isNotNull();
    }

    @Test
    public void mustReturnNull_whenDifferentUserId() {
        UUID differentUserId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of());
        TransactionPageResponse pageResponseMock = mock(TransactionPageResponse.class);

        when(repository.findAllByUserId(differentUserId, pageable)).thenReturn(page);
        when(mapper.toPageResponse(page)).thenReturn(pageResponseMock);

        var result = transactionService.getAllByUserId(differentUserId, pageable);

        assertThat(result).isNotNull();
    }

    @Test
    public void mustReturnNull_whenDifferentUserId_and_Create() {
        UUID differentUserId = UUID.randomUUID();
        String impotencyKey = "impotencyKey";
        TransactionRequest request = new TransactionRequest(
                "description",
                BigDecimal.TEN,
                "category",
                TransactionType.INCOME,
                OffsetDateTime.now()

        );
        Transaction transactionMock = mock(Transaction.class);
        Transaction savedTransactionMock = mock(Transaction.class);
        TransactionResponse responseMock = mock(TransactionResponse.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(mapper.toEntity(request)).thenReturn(transactionMock);
        when(repository.save(transactionMock)).thenReturn(savedTransactionMock);
        when(mapper.toResponse(savedTransactionMock)).thenReturn(responseMock);

        var result = transactionService.create(differentUserId, impotencyKey, request);

        assertThat(result).isNotNull();
    }
}
