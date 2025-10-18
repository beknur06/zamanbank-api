package com.kz.zamanbankapi.service;

import com.kz.zamanbankapi.dao.entities.Transaction;
import com.kz.zamanbankapi.dao.entities.User;
import com.kz.zamanbankapi.dao.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class FraudDetectionService {
    private static final String apiUrl = "https://openai-hub.neuraldeep.tech/v1/chat/completions";
    private static final String apiKey = "sk-roG3OusRr0TLCHAADks6lw";
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

        try {
            user.setIsFraudster(checkFraudWithLLM(reportMessage));
        } catch (Exception e) {
            // Логирование ошибки
            throw new RuntimeException("Ошибка при проверке на мошенничество", e);
        }
    }

    private boolean checkFraudWithLLM(String reportMessage) throws Exception {
        String prompt = String.format(
                "Проанализируй следующее сообщение о подозрении в мошенничестве и определи, является ли это действительно мошенничеством. " +
                        "Ответь только 'true' если это мошенничество или 'false' если нет. Сообщение: %s",
                reportMessage
        );

        String requestBody = String.format(
                "{\"model\":\"gpt-4o-mini\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                prompt.replace("\"", "\\\"")
        );

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofString());

        // Парсинг ответа (используйте библиотеку JSON, например Jackson или Gson)
        String responseContent = parseResponseContent(response.body());

        System.out.println("LLM Response Content: " + responseContent);
        return responseContent.toLowerCase().contains("true");
    }

    private String parseResponseContent(String responseBody) {
        // Используйте Jackson или Gson для парсинга JSON
        // Пример с простым поиском (замените на полноценный парсинг):
        int contentStart = responseBody.indexOf("\"content\":\"") + 11;
        int contentEnd = responseBody.indexOf("\"", contentStart);
        return responseBody.substring(contentStart, contentEnd);
    }
}