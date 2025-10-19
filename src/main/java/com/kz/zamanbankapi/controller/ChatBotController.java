package com.kz.zamanbankapi.controller;

import com.kz.zamanbankapi.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat-bot")
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatBotService chatBotService;

    @PutMapping("/update-goal")
    public ResponseEntity<String> updateFinancialGoal(@RequestParam String financialGoal) {
        chatBotService.updateFinancialGoal(financialGoal);
        return ResponseEntity.ok()
                .body(new JSONObject().put("message", "Financial goal updated successfully").toString());
    }

    @PostMapping("/analyze-speech")
    public ResponseEntity<byte[]> analyzeAndSpeak(@RequestParam String text) {
        try {
            byte[] audioData = chatBotService.analyzeAndConvertToSpeech(text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("audio/mpeg"));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(audioData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam String text) {
        try {
            String response = chatBotService.processMessage(text);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONObject()
                            .put("response", response)
                            .put("message", text)
                            .toString());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new JSONObject().put("error", e.getMessage()).toString());
        }
    }
}