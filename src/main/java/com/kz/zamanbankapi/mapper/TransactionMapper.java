package com.kz.zamanbankapi.mapper;

import com.kz.zamanbankapi.dao.entities.Transaction;
import com.kz.zamanbankapi.dto.TransactionDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {
    Transaction transactionDtoToTransaction(TransactionDto transactionDto);
    TransactionDto transactionToTransactionDto(Transaction transaction);
}
