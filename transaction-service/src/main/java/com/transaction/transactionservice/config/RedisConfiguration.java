package com.transaction.transactionservice.config;

import com.transaction.transactionservice.dto.response.TransactionResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfiguration {

    public static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    public static final String IDEMPOTENCY_PREFIX = "idempotency:transaction:";

    @Bean
    RedisTemplate<String, TransactionResponse> transactionResponseRedisTemplate(RedisConnectionFactory connectionFactory) {
        var serializer = new JacksonJsonRedisSerializer<>(TransactionResponse.class);

        RedisTemplate<String, TransactionResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }
}