package com.transaction.transactionservice.transaction.application.port.in;

import com.transaction.transactionservice.transaction.application.command.TransactionFilter;
import com.transaction.transactionservice.transaction.application.dto.PageResponse;
import com.transaction.transactionservice.transaction.application.dto.TransactionResponse;
import org.springframework.data.domain.Pageable;

public interface ListTransactionsUseCase {
    PageResponse<TransactionResponse> list(TransactionFilter filter, Pageable pageable);
}
