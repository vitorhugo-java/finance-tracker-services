package com.transaction.transactionservice.transaction.infrastructure.idempotency;

import com.transaction.transactionservice.transaction.application.dto.TransactionResponse;
import com.transaction.transactionservice.transaction.application.port.out.IdempotencyPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
public class RedisIdempotencyAdapter implements IdempotencyPort {

    private static final Duration TTL = Duration.ofHours(24);
    private static final String RESPONSE_PREFIX = "idempotency:transactions:create:response:";
    private static final String LOCK_PREFIX = "idempotency:transactions:create:lock:";

    private final RedisTemplate<String, TransactionResponse> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisIdempotencyAdapter(RedisTemplate<String, TransactionResponse> redisTemplate,
                                   StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Optional<TransactionResponse> find(UUID userId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(redisTemplate.opsForValue().get(responseKey(userId, idempotencyKey)));
    }

    @Override
    public boolean markProcessing(UUID userId, String idempotencyKey) {
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey(userId, idempotencyKey), "PROCESSING", TTL);
        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public void save(UUID userId, String idempotencyKey, TransactionResponse response) {
        redisTemplate.opsForValue().set(responseKey(userId, idempotencyKey), response, TTL);
        release(userId, idempotencyKey);
    }

    @Override
    public void release(UUID userId, String idempotencyKey) {
        stringRedisTemplate.delete(lockKey(userId, idempotencyKey));
    }

    private String responseKey(UUID userId, String idempotencyKey) {
        return RESPONSE_PREFIX + userId + ":" + idempotencyKey;
    }

    private String lockKey(UUID userId, String idempotencyKey) {
        return LOCK_PREFIX + userId + ":" + idempotencyKey;
    }
}
