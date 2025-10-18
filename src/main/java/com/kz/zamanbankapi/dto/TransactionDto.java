package com.kz.zamanbankapi.dto;

import com.kz.zamanbankapi.dao.entities.Card;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private Long id;
    private BigDecimal amount;
    private String message;
    private Boolean isSender;
    private LocalDateTime createdAt = LocalDateTime.now();
}
