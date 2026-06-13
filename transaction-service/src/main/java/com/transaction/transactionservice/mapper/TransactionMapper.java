package com.transaction.transactionservice.mapper;

import com.transaction.transactionservice.dto.request.CreateTransactionRequest;
import com.transaction.transactionservice.dto.response.TransactionPageResponse;
import com.transaction.transactionservice.dto.response.TransactionResponse;
import com.transaction.transactionservice.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
        componentModel = "spring"
)
public interface TransactionMapper {

    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "userId",
            ignore = true
    )
    @Mapping(
            target = "status",
            ignore = true
    )
    @Mapping(
            target = "createdAt",
            ignore = true
    )
    @Mapping(
            target = "updatedAt",
            ignore = true
    )
    Transaction toEntity(
            CreateTransactionRequest request
    );

    TransactionResponse toResponse(
            Transaction transaction
    );

    @Mapping(target = "pageNumber", source = "number")
    @Mapping(target = "pageSize", source = "size")
    TransactionPageResponse toPageResponse(
            Page<Transaction> transaction
    );
}