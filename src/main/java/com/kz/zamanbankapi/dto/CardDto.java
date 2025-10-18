package com.kz.zamanbankapi.dto;

import com.kz.zamanbankapi.dao.enums.AccountType;
import com.kz.zamanbankapi.dao.enums.CardType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CardDto {
    private Long id;
    private String cardNumber;
    private String cardHolderName;
    private LocalDate expirationDate;
    private String cvv;
    private CardType cardType;
    private BigDecimal balance;
    private String currency;
    private AccountType accountType;
    private List<TransactionDto> transactions;
    private String designImageUrl;
}
