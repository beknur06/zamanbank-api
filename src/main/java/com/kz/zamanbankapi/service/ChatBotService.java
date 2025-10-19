package com.kz.zamanbankapi.service;

import com.kz.zamanbankapi.dao.entities.Card;
import com.kz.zamanbankapi.dao.entities.Transaction;
import com.kz.zamanbankapi.dao.entities.User;
import com.kz.zamanbankapi.dao.repositories.TransactionRepository;
import com.kz.zamanbankapi.dao.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBotService {

    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";
    @Value("${openai.api-key}")
    private String apiKey;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final TextToSpeechService textToSpeechService;
    private final TransactionRepository transactionRepository;

    private final UserService userService;

    public void updateFinancialGoal(String financialGoal) {
        User user = userService.getCurrentUser();
        user.setFinancialGoal(financialGoal);
        userRepository.save(user);
    }

    public String processMessage(String userMessage) {
        // Определяем тип запроса через GPT
        String requestType = detectRequestType(userMessage);

        log.info("requestType: {}", requestType);

        // Маршрутизируем на соответствующий обработчик
        return switch (requestType.toLowerCase()) {
            case "report" -> generateFinancialReport(userMessage);
            case "analysis" -> analyzeFinances(userMessage);
            case "advice" -> askFinancialAdvice(userMessage);
            default -> getDirectChatResponse(userMessage);
        };
    }

    private String getDirectChatResponse(String userMessage) {
        User user = userService.getCurrentUser();
        String financialGoal = user.getFinancialGoal();

        String systemPrompt = "You are a friendly financial advisor having a casual conversation. " +
                "Speak naturally like you're chatting with a friend. Don't use any formatting, " +
                "numbered lists, bullet points, or special characters. Just plain conversational text. " +
                "Keep your advice warm, personal, and easy to understand.";

        String contextualMessage = String.format(
                "The user's financial goal is: %s\n\nUser asks: %s",
                financialGoal != null ? financialGoal : "not yet set",
                userMessage
        );

        JSONObject systemMessage = new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt);

        JSONObject userMsg = new JSONObject()
                .put("role", "user")
                .put("content", contextualMessage);

        JSONObject body = new JSONObject()
                .put("model", "gpt-4o-mini")
                .put("messages", new JSONArray().put(systemMessage).put(userMsg))
                .put("max_tokens", 1000)
                .put("temperature", 0.8);

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

        throw new RuntimeException("Ошибка получения ответа от AI");
    }

    private String detectRequestType(String userMessage) {
        String prompt = String.format(
                """
                        Classify the following user message into one of these categories:
                        - 'report': User wants a comprehensive financial report/overview/summary
                        - 'analysis': User wants to analyze specific financial data/transactions/expenses
                        - 'advice': User asks a question or needs financial advice/guidance
                        
                        User message: "%s"
                        
                        Respond with ONLY one word: report, analysis, or advice""",
                userMessage
        );

        JSONObject message = new JSONObject()
                .put("role", "user")
                .put("content", prompt);

        JSONObject body = new JSONObject()
                .put("model", "gpt-4o-mini")
                .put("messages", new JSONArray().put(message))
                .put("max_tokens", 1000)
                .put("temperature", 0.3);

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

        return "advice";
    }

    public byte[] analyzeAndConvertToSpeech(String text) {
        User user = userService.getCurrentUser();
        String financialGoal = user.getFinancialGoal();

        String aiResponse = analyzeWithAI(text, financialGoal);
        return textToSpeechService.convertToSpeech(aiResponse);
    }

    public String generateFinancialReport(String reportMessage) {
        User user = userService.getCurrentUser();
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
        String prompt = String.format(
                """
                        User's financial goal: %s
                        
                        User's message: %s
                        
                        Recent transactions:
                        %s
                        
                        Amount of money in the card: %.2f
                        
                        Generate a comprehensive financial report covering:
                        1. Current financial status assessment
                        2. Progress towards the goal
                        3. Key recommendations
                        4. Action items
                        5. Do not format or say hi just see it as chat message
                        Keep it concise and actionable (max 100 words) and do not use markdown.""",
                financialGoal != null ? financialGoal : "Not specified",
                reportMessage,
                formatTransactions(transactionList),
                userCard.getBalance()
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
        User user = userService.getCurrentUser();
        String financialGoal = user.getFinancialGoal();

        String prompt = String.format(
                """
                        User's financial goal: %s
                        Financial data to analyze: %s
                        Amount of money in the card: %.2f
                        Provide detailed financial analysis:
                        1. Spending patterns
                        2. Savings rate
                        3. Risk assessment
                        4. Optimization opportunities
                        5. Do not format anything with text just see it as chat message
                        Be specific and data-driven.
                        Keep it concise and actionable (max 70 words) and do not use markdown""",
                financialGoal != null ? financialGoal : "Not specified",
                financialData,
                user.getCards().stream().findFirst().map(Card::getBalance).orElse(BigDecimal.valueOf(0.0))
        );

        return getAIResponse(prompt, 1000);
    }

    public String askFinancialAdvice(String question) {
        User user = userService.getCurrentUser();
        String financialGoal = user.getFinancialGoal();

        String prompt = String.format(
                """
                        User's financial goal: %s
                        
                        User's question: %s
                        
                        Provide clear, actionable financial advice. \
                        Be specific, practical, and tailored to their goal. \
                        Include step-by-step guidance if applicable.\
                        Do not format anything with text just see it as chat message
                        Keep it concise and actionable (max 70 words) and do not use markdown
                        """,
                financialGoal != null ? financialGoal : "Not specified",
                question
        );

        return getAIResponse(prompt, 1000);
    }

    private String analyzeWithAI(String userText, String financialGoal) {
        String prompt = String.format(
                """
                        User's financial goal: %s
                        
                        User's message: %s
                        
                        Analyze the user's message in context of their financial goal. \
                        Provide brief, actionable financial advice (max 2-3 sentences), do not use markdown.""",
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

        throw new RuntimeException("Ошибка получения ответа от AI");
    }
}