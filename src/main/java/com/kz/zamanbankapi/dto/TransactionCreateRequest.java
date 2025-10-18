package com.kz.zamanbankapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionCreateRequest {
    @NotNull
    private Long senderCardId;

    private String receiverPhone;
    private String receiverCardNumber;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private String message;
}