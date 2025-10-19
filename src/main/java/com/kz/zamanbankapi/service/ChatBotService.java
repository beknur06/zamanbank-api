package com.kz.zamanbankapi.service;

import com.kz.zamanbankapi.dao.entities.Card;
import com.kz.zamanbankapi.dao.entities.Transaction;
import com.kz.zamanbankapi.dao.entities.User;
import com.kz.zamanbankapi.dao.repositories.TransactionRepository;
import com.kz.zamanbankapi.dao.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatBotService {

    private static final String OPENAI_CHAT_URL = "https://openai-hub.neuraldeep.tech/v1/chat/completions";
    private static final String API_KEY = "sk-roG3OusRr0TLCHAADks6lw";

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final TextToSpeechService textToSpeechService;
    private final TransactionRepository transactionRepository;

    public void updateFinancialGoal(String financialGoal) {
        User user = getCurrentUser();
        user.setFinancialGoal(financialGoal);
        userRepository.save(user);
    }

    public String processMessage(String userMessage) {
        // Определяем тип запроса через GPT
        String requestType = detectRequestType(userMessage);

        // Маршрутизируем на соответствующий обработчик
        return switch (requestType.toLowerCase()) {
            case "report" -> generateFinancialReport();
            case "analysis" -> analyzeFinances(userMessage);
            case "advice" -> askFinancialAdvice(userMessage);
            default -> askFinancialAdvice(userMessage); // По умолчанию - совет
        };
    }

    private String detectRequestType(String userMessage) {
        String prompt = String.format(
                "Classify the following user message into one of these categories:\n" +
                        "- 'report': User wants a comprehensive financial report/overview/summary\n" +
                        "- 'analysis': User wants to analyze specific financial data/transactions/expenses\n" +
                        "- 'advice': User asks a question or needs financial advice/guidance\n\n" +
                        "User message: \"%s\"\n\n" +
                        "Respond with ONLY one word: report, analysis, or advice",
                userMessage
        );

        JSONObject message = new JSONObject()
                .put("role", "user")
                .put("content", prompt);

        JSONObject body = new JSONObject()
                .put("model", "gpt-4o-mini")
                .put("messages", new JSONArray().put(message))
                .put("max_tokens", 10)
                .put("temperature", 0.3);

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

        return "advice";
    }

    public byte[] analyzeAndConvertToSpeech(String text) {
        User user = getCurrentUser();
        String financialGoal = user.getFinancialGoal();

        String aiResponse = analyzeWithAI(text, financialGoal);
        return textToSpeechService.convertToSpeech(aiResponse);
    }

    public String generateFinancialReport() {
        User user = getCurrentUser();
        String financialGoal = user.getFinancialGoal();
        Card userCard = user.getCards().stream()
                .max(Comparator.comparing(card -> {
                    List<Transaction> transactions = transactionRepository.findAllBySenderCardId(card.getId());
                    return transactions.stream()
                            .map(Transaction::getCreatedAt)
                            .max(Comparator.naturalOrder())
                            .orElse(null);
                }, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow(() -> new RuntimeException("У пользователя нет карт"));

        List<Transaction> transactionList = transactionRepository.findAllBySenderCardId(userCard.getId());

        System.out.println("Generating report for financial goal: " + financialGoal);

        String prompt = String.format(
                "User's financial goal: %s\n\n" +
                        "Recent transactions:\n%s\n\n" +
                        "Generate a comprehensive financial report covering:\n" +
                        "1. Current financial status assessment\n" +
                        "2. Progress towards the goal\n" +
                        "3. Key recommendations\n" +
                        "4. Action items\n" +
                        "Keep it concise and actionable (max 100 words).",
                financialGoal != null ? financialGoal : "Not specified",
                formatTransactions(transactionList)
        );

        return getAIResponse(prompt, 1000);
    }

    private String formatTransactions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return "No transactions available";
        }

        StringBuilder sb = new StringBuilder();
        transactions.stream()
                .limit(10)
                .forEach(t -> sb.append(String.format(
                        "- %s: %.2f\n",
                        t.getCreatedAt(),
                        t.getAmount()
                )));

        return sb.toString();
    }

    public String analyzeFinances(String financialData) {
        User user = getCurrentUser();
        String financialGoal = user.getFinancialGoal();

        String prompt = String.format(
                "User's financial goal: %s\n\n" +
                "Financial data to analyze: %s\n\n" +
                "Provide detailed financial analysis:\n" +
                "1. Spending patterns\n" +
                "2. Savings rate\n" +
                "3. Risk assessment\n" +
                "4. Optimization opportunities\n" +
                "Be specific and data-driven.",
                financialGoal != null ? financialGoal : "Not specified",
                financialData
        );

        return getAIResponse(prompt, 1000);
    }

    public String askFinancialAdvice(String question) {
        User user = getCurrentUser();
        String financialGoal = user.getFinancialGoal();

        String prompt = String.format(
                "User's financial goal: %s\n\n" +
                "User's question: %s\n\n" +
                "Provide clear, actionable financial advice. " +
                "Be specific, practical, and tailored to their goal. " +
                "Include step-by-step guidance if applicable.",
                financialGoal != null ? financialGoal : "Not specified",
                question
        );

        return getAIResponse(prompt, 1000);
    }

    private String analyzeWithAI(String userText, String financialGoal) {
        String prompt = String.format(
                "User's financial goal: %s\n\n" +
                "User's message: %s\n\n" +
                "Analyze the user's message in context of their financial goal. " +
                "Provide brief, actionable financial advice (max 2-3 sentences).",
                financialGoal != null ? financialGoal : "Not specified",
                userText
        );

        return getAIResponse(prompt, 1000);
    }

    private String getAIResponse(String prompt, int maxTokens) {
        JSONObject message = new JSONObject()
                .put("role", "user")
                .put("content", prompt);

        JSONObject body = new JSONObject()
                .put("model", "gpt-4o-mini")
                .put("messages", new JSONArray().put(message))
                .put("max_tokens", maxTokens)
                .put("temperature", 0.7);

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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}