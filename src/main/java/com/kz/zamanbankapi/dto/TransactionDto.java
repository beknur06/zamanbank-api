package com.kz.zamanbankapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String description;
    private String transactionType;
}
