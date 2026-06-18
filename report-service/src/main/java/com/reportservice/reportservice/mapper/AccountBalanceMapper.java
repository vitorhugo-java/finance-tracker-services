package com.reportservice.reportservice.mapper;

import com.reportservice.reportservice.dto.AccountBalanceResponse;
import com.reportservice.reportservice.entity.AccountBalance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring"
)
public interface AccountBalanceMapper {

    AccountBalanceResponse toResponse(
            AccountBalance accountBalance
    );
}
