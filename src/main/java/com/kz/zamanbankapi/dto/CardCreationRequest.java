package com.kz.zamanbankapi.dto;

import com.kz.zamanbankapi.dao.enums.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardCreationRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Card name is required")
    private String cardName;

    @NotBlank(message = "Card type is required")
    private CardType cardType;

    @NotNull
    private String currency;

    private String designPreferences;
}