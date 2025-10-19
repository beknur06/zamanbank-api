package com.kz.zamanbankapi.service;

import com.kz.zamanbankapi.dao.entities.Transaction;
import com.kz.zamanbankapi.dao.entities.User;
import com.kz.zamanbankapi.dao.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FraudDetectionService {
    private static final String apiUrl = "https://api.openai.com/v1/chat/completions";
    @Value("${openai.api-key}")
    private String apiKey;
    private final UserRepository userRepository;

    public FraudDetectionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean getFraud(String phoneNumber) {
        User user = userRepository.findByUsername(phoneNumber).orElse(null);
        return user != null && user.getIsFraudster();
    }

    public void reportFraud(Transaction transaction) {
        User user = transaction.getReceiverCard().getUser();
        String reportMessage = transaction.getReport();

        log.info(reportMessage);
        try {
            user.setIsFraudster(checkFraudWithLLM(reportMessage));
        } catch (Exception e) {
            // Логирование ошибки
            throw new RuntimeException("Ошибка при проверке на мошенничество", e);
        }
    }

    private boolean checkFraudWithLLM(String reportMessage) throws Exception {
        String prompt = String.format(
                "Analyze the following message about suspected fraud and determine if it is actually fraud. " +
                        "Answer only 'true' if it is fraud or 'false' if not. Message: %s",
                reportMessage
        );

        String requestBody = String.format(
                "{\"model\":\"gpt-4o-mini\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                prompt.replace("\"", "\\\"")
        );

        log.info(prompt);

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofString());

        log.info("LLM Response: {}", response.body());
        // Парсинг ответа (используйте библиотеку JSON, например Jackson или Gson)
        String responseContent = parseResponseContent(response.body());

        log.info("LLM Response Content: {}", responseContent);
        return responseContent.toLowerCase().contains("true");
    }

    private String parseResponseContent(String responseBody) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Ошибка при парсинге ответа LLM: {}", e.getMessage());
            throw new RuntimeException("Не удалось распарсить ответ от LLM", e);
        }
    }
}