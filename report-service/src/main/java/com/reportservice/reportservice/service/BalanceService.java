package com.reportservice.reportservice.service;

import com.reportservice.reportservice.dto.AccountBalanceResponse;
import com.reportservice.reportservice.event.record.TransactionCreatedEvent;
import com.reportservice.reportservice.event.record.TransactionType;
import com.reportservice.reportservice.mapper.AccountBalanceMapper;
import com.reportservice.reportservice.repository.AccountBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final AccountBalanceRepository repository;
    private final AccountBalanceMapper mapper;

    @Transactional
    public void applyTransaction(TransactionCreatedEvent event) {
        var delta = event.type() == TransactionType.EXPENSE
                ? event.amount().negate()
                : event.amount();

        repository.applyDelta(event.userId(), delta);
    }

    @Transactional
    public AccountBalanceResponse getReport(UUID userid) {
        var balance = repository.findById(userid).orElseThrow(() -> new RuntimeException("User not found"));
        return mapper.toResponse(balance);
    }
}
