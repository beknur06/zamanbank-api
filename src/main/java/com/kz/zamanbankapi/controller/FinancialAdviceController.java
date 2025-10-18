package com.kz.zamanbankapi.controller;

import com.kz.zamanbankapi.dao.entities.Card;
import com.kz.zamanbankapi.dao.entities.User;
import com.kz.zamanbankapi.dao.repositories.CardRepository;
import com.kz.zamanbankapi.dao.repositories.UserRepository;
import com.kz.zamanbankapi.dto.FinancialAdviceRequest;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/financial-advice")
@RequiredArgsConstructor
public class FinancialAdviceController {

    private static final String OPENAI_CHAT_URL = "https://openai-hub.neuraldeep.tech/v1/chat/completions";
    private static final String OPENAI_TTS_URL = "https://openai-hub.neuraldeep.tech/audio/speech";
    private static final String API_KEY = "sk-roG3OusRr0TLCHAADks6lw";

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @PutMapping("/update-goal")
    public void updateFinancialGoal(@RequestParam String financialGoal) {
        User user = getCurrentUser();
        user.setFinancialGoal(financialGoal);
        userRepository.save(user);
    }

    @PostMapping("/analyze-speech")
    public ResponseEntity<byte[]> analyzeAndSpeak(@RequestParam String text) {
        try {
            User user = getCurrentUser();
            String financialGoal = user.getFinancialGoal();

            // 1. Анализ текста с учетом финансовой цели
            String aiResponse = analyzeWithAI(text, financialGoal);

            // 2. Конвертация ответа в речь
            byte[] audioData = convertToSpeech(aiResponse);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("audio/mpeg"));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(audioData);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при анализе и генерации речи", e);
        }
    }

    private String analyzeWithAI(String userText, String financialGoal) {
        JSONObject message = new JSONObject()
                .put("role", "user")
                .put("content", String.format(
                        "User's financial goal: %s\n\n" +
                                "User's message: %s\n\n" +
                                "Analyze the user's message in context of their financial goal. " +
                                "Provide brief, actionable financial advice (max 2-3 sentences).",
                        financialGoal != null ? financialGoal : "Not specified",
                        userText
                ));

        JSONObject body = new JSONObject()
                .put("model", "gpt-4o-mini")
                .put("messages", new JSONArray().put(message))
                .put("max_tokens", 150);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                OPENAI_CHAT_URL,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            return jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();
        }

        throw new RuntimeException("Ошибка получения ответа от AI");
    }

    private byte[] convertToSpeech(String text) {
        String url = "https://api.deepgram.com/v1/speak?model=aura-2-helena-en";

        JSONObject body = new JSONObject()
                .put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token 3a7bc23c72a39d15b7d3c8a7cbe199df4dfd69c1");

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                byte[].class
        );

        return response.getBody();
    }
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElse(null);
    }
}
