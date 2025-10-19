package com.kz.zamanbankapi.service;

import com.kz.zamanbankapi.dao.entities.Card;
import com.kz.zamanbankapi.dao.entities.Transaction;
import com.kz.zamanbankapi.dao.entities.User;
import com.kz.zamanbankapi.dao.repositories.TransactionRepository;
import jdk.jfr.Registered;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {
    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";
    @Value("${openai.api-key}")
    private String apiKey;

    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    public String getReminders() {
        User user = userService.getCurrentUser();
        log.info(user.toString());
        log.info(user.getCards().toString());
        if (user.getCards().isEmpty()) {
            return "Assalamu alaikum";
        }

        // Получаем транзакции пользователя
        Card userCard = user.getCards().stream().findFirst().orElseThrow();
        List<Transaction> transactions = transactionRepository.findAllBySenderCardId(userCard.getId());

        // Формируем промпт для GPT
        String prompt = String.format(
                """
                        Financial goal: %s
                        Recent transactions: %s
                        Balance: %.2f
                        
                        Generate a SHORT reminder (3-5 words ONLY) about finances.
                        Do not put dot at the end.
                        """,
                user.getFinancialGoal() != null ? user.getFinancialGoal() : "Not set",
                formatTransactionsForReminder(transactions),
                userCard.getBalance()
        );

        return getShortReminderFromGPT(prompt);
    }

    private String formatTransactionsForReminder(List<Transaction> transactions) {
        if (transactions.isEmpty()) return "No transactions";

        return transactions.stream()
                .limit(5)
                .map(t -> String.format("%.2f on %s", t.getAmount(), t.getCreatedAt()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private String getShortReminderFromGPT(String prompt) {
        JSONObject message = new JSONObject()
                .put("role", "user")
                .put("content", prompt);

        JSONObject body = new JSONObject()
                .put("model", "gpt-4o-mini")
                .put("messages", new JSONArray().put(message))
                .put("max_tokens", 20)
                .put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

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

        return "Check your finances";
    }
}
