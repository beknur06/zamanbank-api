package com.kz.zamanbankapi.controller;

import com.kz.zamanbankapi.dto.CardCreationRequest;
import com.kz.zamanbankapi.dto.CardDto;
import com.kz.zamanbankapi.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping("/generate-new")
    public ResponseEntity<CardDto> generateCardImages(@RequestBody CardCreationRequest request) {
        return ResponseEntity.ok(cardService.generateAndStoreCard(request));
    }
}
