package com.transaction.transactionservice.transaction.application.port.in;

import com.transaction.transactionservice.transaction.application.command.CreateTransactionCommand;
import com.transaction.transactionservice.transaction.application.dto.TransactionResponse;

public interface CreateTransactionUseCase {
    TransactionResponse create(CreateTransactionCommand command);
}
