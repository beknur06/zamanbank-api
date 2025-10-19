package com.kz.zamanbankapi.mapper;

import com.kz.zamanbankapi.dao.entities.Card;
import com.kz.zamanbankapi.dao.entities.Transaction;
import com.kz.zamanbankapi.dto.TransactionDto;
import com.kz.zamanbankapi.dto.TransactionDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {
    @Mapping(target = "isSender", expression = "java(transaction.getSenderCard().getId().equals(userCardId))")
    TransactionDto toDto(Transaction transaction, @Context Long userCardId);
    TransactionDto toDto(Transaction transaction);
    Transaction toEntity(TransactionDto transactionDto);
}