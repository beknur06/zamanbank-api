package com.kz.zamanbankapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardCreationRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    private String designPreferences;
}