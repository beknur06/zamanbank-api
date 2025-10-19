package com.kz.zamanbankapi.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TextToSpeechService {

    private static final String TTS_URL = "https://api.openai.com/v1/audio/speech";
    @Value("${openai.api-key}")
    private String apiKey;
    private final RestTemplate restTemplate;

    public byte[] convertToSpeech(String text) {
        JSONObject body = new JSONObject()
                .put("model", "gpt-4o-mini-tts")
                .put("input", text)
                .put("voice", "shimmer")
                .put("response_format", "aac"); // Изменено на malon

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<byte[]> response = restTemplate.postForEntity(
                TTS_URL,
                entity,
                byte[].class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }

        throw new RuntimeException("Ошибка преобразования текста в речь");
    }
}