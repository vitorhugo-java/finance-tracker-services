package com.transaction.transactionservice.config;

import com.transaction.transactionservice.dto.response.TransactionResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

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