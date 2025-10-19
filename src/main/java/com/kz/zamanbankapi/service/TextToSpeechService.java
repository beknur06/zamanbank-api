package com.kz.zamanbankapi.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TextToSpeechService {

    private static final String DEEPGRAM_URL = "https://api.deepgram.com/v1/speak?model=aura-2-helena-en";
    private static final String DEEPGRAM_API_KEY = "3a7bc23c72a39d15b7d3c8a7cbe199df4dfd69c1";

    private final RestTemplate restTemplate;

    public byte[] convertToSpeech(String text) {
        JSONObject body = new JSONObject()
                .put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + DEEPGRAM_API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                DEEPGRAM_URL,
                HttpMethod.POST,
                entity,
                byte[].class
        );

        return response.getBody();
    }
}