package com.transaction.transactionservice.service;

import com.transaction.transactionservice.dto.request.CreateTransactionRequest;
import com.transaction.transactionservice.dto.response.TransactionPageResponse;
import com.transaction.transactionservice.dto.response.TransactionResponse;
import com.transaction.transactionservice.entity.Transaction;
import com.transaction.transactionservice.event.TransactionEventPublisher;
import com.transaction.transactionservice.mapper.TransactionMapper;
import com.transaction.transactionservice.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.transaction.transactionservice.config.RedisConfiguration.IDEMPOTENCY_PREFIX;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final TransactionEventPublisher publisher;
    private final RedisTemplate<String, TransactionResponse> redisTemplate;

    public TransactionResponse create(UUID userId, String impotencyKey, CreateTransactionRequest request) {
        String redisKey = IDEMPOTENCY_PREFIX + impotencyKey;
        TransactionResponse cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            Transaction transaction = mapper.toEntity(cached);
            transaction.setCached(true);
            return mapper.toResponse(transaction);
        }

        Transaction transaction = mapper.toEntity(request);
        transaction.setUserId(userId);
        Transaction saved = repository.save(transaction);

        publisher.publishCreated(saved);

        TransactionResponse response = mapper.toResponse(saved);
        redisTemplate.opsForValue().set(redisKey, response);

        return mapper.toResponse(saved);
    }

    public TransactionPageResponse getAllByUserId(UUID userId, Pageable pageable) {
        return mapper.toPageResponse(repository.findAllByUserId(userId, pageable));
    }
}
