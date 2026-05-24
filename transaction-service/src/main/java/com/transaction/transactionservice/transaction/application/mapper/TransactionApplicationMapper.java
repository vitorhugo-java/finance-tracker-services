package com.transaction.transactionservice.transaction.application.mapper;

import com.transaction.transactionservice.transaction.application.dto.TransactionResponse;
import com.transaction.transactionservice.transaction.domain.model.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionApplicationMapper {
    TransactionResponse toResponse(Transaction transaction);
}
