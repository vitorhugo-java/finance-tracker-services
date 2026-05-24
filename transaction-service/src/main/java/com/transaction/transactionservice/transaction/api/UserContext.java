package com.transaction.transactionservice.transaction.api;

import com.transaction.transactionservice.transaction.domain.exception.ValidationException;

import java.util.UUID;

public record UserContext(UUID userId, String email, String roles) {

    static UserContext fromHeaders(String userId, String email, String roles) {
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("X-User-Id header is required");
        }
        try {
            return new UserContext(UUID.fromString(userId), email, roles);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("X-User-Id header must be a valid UUID");
        }
    }
}
