package com.kz.zamanbankapi.service;

import com.kz.zamanbankapi.dao.entities.User;
import com.kz.zamanbankapi.dao.enums.CardType;
import com.kz.zamanbankapi.dto.CardCreationRequest;
import com.kz.zamanbankapi.dto.CardDto;
import com.kz.zamanbankapi.dao.entities.Card;
import com.kz.zamanbankapi.mapper.CardMapper;
import com.kz.zamanbankapi.repositories.CardRepository;
import com.kz.zamanbankapi.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    private final RestTemplate restTemplate;
    private final CardMapper cardMapper;

    public CardDto generateAndStoreCard(CardCreationRequest cardRequest) {
        User user = userRepository.findById(cardRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        String designUrl = generateCardDesignAndSaveToMinio(cardRequest.getDesignPreferences());

        Card card = new Card();
        card.setUser(user);
        card.setCardNumber(generateCardNumber(CardType.MASTERCARD));
        card.setCardHolderName(user.getName() + " " + user.getSurname());
        card.setCvv(generateCVV());
        card.setExpirationDate(generateExpiryEom(2));
        card.setDesignImageUrl(designUrl);
        card.setCardType(CardType.MASTERCARD);
        card.setCurrency("KZT");
        card.setBalance(BigDecimal.valueOf(0.0));

        Card savedCard = cardRepository.save(card);
        return cardMapper.toCardDto(savedCard);
    }

    private String generateCardDesignAndSaveToMinio(String designPreferences) {
        try {
            String apiUrl = "https://platform.higgsfield.ai/v1/text2image/nano-banana";

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("hf-api-key", "a0369d27-fddf-4a69-9ad3-7a0cf456b77d");
            headers.set("hf-secret", "4f5ad6fbb4bd91f9fc56c2296e38612a26c9953da9ec4686a1580858be9957df");
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            String prompt = "Generate a polished, production-ready debit card front using the provided input image as the background or starting layout. CRITICAL: The card must fill the entire image frame with NO white background, NO padding, and NO space around the card edges. The card itself should extend to all edges of the image. Mandatory requirements (these must always be present and visible): 1) ALWAYS KEEP the Bank logo in provided image, placed in the top-left 2) ALWAYS KEEP the Mastercard logo in the bottom-right corner (use authentic Mastercard mark proportions, do NOT change Mastercard colors); 3) ALWAYS KEEP the text 'platinum debit' placed prominently in the top-right. Layout rules: the card must fill the entire canvas edge-to-edge with no background visible outside the card boundaries; no required elements may be clipped. Style rules: follow user preferences when safe; if they conflict with mandatory elements, prioritize the mandatory elements. Safety: never render PAN/CVV/expiry or unsafe content. Output: full-frame card that fills the entire image with no surrounding background or whitespace; always return a complete card image even if the user's prompt is vague or contradictory. Do not add any data on the card, including numbers, user name etc. Here user preferences: " + designPreferences;

            String requestBody = String.format("""
            {
              "params": {
                "aspect_ratio": "16:9",
                "input_images": [
                  {
                    "type": "image_url",
                    "image_url": "https://d3snorpfx4xhv8.cloudfront.net/9e75a23e-a91d-41ae-b65b-f1c8cad718dd/91ecc85d-8ef5-4fb1-88f6-fa6a84e7eee9.png",
                    "role": "background"
                  }
                ],
                "prompt": "%s"
              }
            }
            """, prompt.replace("\"", "\\\""));

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    org.springframework.http.HttpMethod.POST,
                    entity,
                    String.class
            );

            UUID jobId = UUID.fromString(
                    new com.fasterxml.jackson.databind.ObjectMapper()
                            .readTree(response.getBody())
                            .get("id")
                            .asText()
            );

            String statusUrl = "https://platform.higgsfield.ai/v1/job-sets/" + jobId;
            org.springframework.http.HttpHeaders statusHeaders = new org.springframework.http.HttpHeaders();
            statusHeaders.set("hf-api-key", "a0369d27-fddf-4a69-9ad3-7a0cf456b77d");
            statusHeaders.set("hf-secret", "4f5ad6fbb4bd91f9fc56c2296e38612a26c9953da9ec4686a1580858be9957df");

            org.springframework.http.HttpEntity<Void> statusEntity = new org.springframework.http.HttpEntity<>(statusHeaders);

            for (int i = 0; i < 60;i++) {
                Thread.sleep(5000);

                org.springframework.http.ResponseEntity<String> statusResponse = restTemplate.exchange(
                        statusUrl,
                        org.springframework.http.HttpMethod.GET,
                        statusEntity,
                        String.class
                );

                com.fasterxml.jackson.databind.JsonNode jobs = new com.fasterxml.jackson.databind.ObjectMapper().readTree(statusResponse.getBody()).get("jobs");

                System.out.println(statusResponse.getBody());
                if (jobs != null && jobs.isArray() && !jobs.isEmpty()) {
                    com.fasterxml.jackson.databind.JsonNode firstJob = jobs.get(0);
                    String status = firstJob.get("status").asText();

                    if ("completed".equals(status)) {
                        return firstJob.get("results").get("min").get("url").asText();
                    }
                }
            }
            return "https://d3snorpfx4xhv8.cloudfront.net/9e75a23e-a91d-41ae-b65b-f1c8cad718dd/91ecc85d-8ef5-4fb1-88f6-fa6a84e7eee9.png";
        } catch (Exception e) {
            // В случае ошибки возвращаем дефолтный дизайн
            return "https://d3snorpfx4xhv8.cloudfront.net/9e75a23e-a91d-41ae-b65b-f1c8cad718dd/91ecc85d-8ef5-4fb1-88f6-fa6a84e7eee9.png";
        }
    }

    private String generateCardNumber(CardType cardType) {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder();

        String prefix = switch (cardType) {
            case VISA -> "4";
            case MASTERCARD -> "5" + random.nextInt(5);
            default -> "4"; // По умолчанию Visa
        };

        cardNumber.append(prefix);

        int remainingDigits = 16 - prefix.length();
        for (int i = 0; i < remainingDigits; i++) {
            cardNumber.append(random.nextInt(10));
        }

        return cardNumber.toString();
    }

    private static LocalDate generateExpiryEom(int yearsAhead) {
        return YearMonth.now().plusYears(yearsAhead).atEndOfMonth();
    }
    private static String generateCVV() {
        int n;
        Random random = new Random();
        do { n = random.nextInt(1000); } while (n == 0); // avoid "000"
        return String.format("%03d", n);
    }
}