package com.reportservice.reportservice.service;

import com.reportservice.reportservice.event.record.TransactionCreatedEvent;
import com.reportservice.reportservice.event.record.TransactionType;
import com.reportservice.reportservice.repository.AccountBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final AccountBalanceRepository repository;

    @Transactional
    public void applyTransaction(TransactionCreatedEvent event) {
        var delta = event.type() == TransactionType.EXPENSE
                ? event.amount().negate()
                : event.amount();

        repository.applyDelta(event.userId(), delta);
    }
}
