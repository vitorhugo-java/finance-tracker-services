package com.transaction.transactionservice.transaction.application.port.in;

import com.transaction.transactionservice.transaction.application.command.UpdateTransactionCommand;
import com.transaction.transactionservice.transaction.application.dto.TransactionResponse;

public interface UpdateTransactionUseCase {
    TransactionResponse update(UpdateTransactionCommand command);
}
