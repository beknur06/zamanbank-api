package com.kz.zamanbankapi.controller;

import com.kz.zamanbankapi.dto.CardCreationRequest;
import com.kz.zamanbankapi.dto.CardDto;
import com.kz.zamanbankapi.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "API для управления банковскими картами")
public class CardController {
    private final CardService cardService;

    @PostMapping
    @Operation(summary = "Создать новую карту", description = "Генерирует и сохраняет новую банковскую карту для пользователя")
    public ResponseEntity<CardDto> createCard(@RequestBody CardCreationRequest request) {
        CardDto card = cardService.generateAndStoreCard(request);
        return ResponseEntity.ok(card);
    }

    @GetMapping
    @Operation(summary = "Получить карты пользователя", description = "Возвращает все банковские карты, связанные с пользователем")
    public ResponseEntity<List<CardDto>> getAllCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }
}
