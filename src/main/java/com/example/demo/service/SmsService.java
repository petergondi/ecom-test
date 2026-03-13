package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class SmsService {

    @Value("${africastalking.api-key}")
    private String apiKey;

    @Value("${africastalking.username}")
    private String username;

    @Value("${africastalking.sender-id:}")
    private String senderId;

    private static final String AT_URL = "https://api.africastalking.com/version1/messaging";

    public void sendSms(String phoneNumber, String message) {
        try {
            String formData = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                    + "&to=" + URLEncoder.encode(phoneNumber, StandardCharsets.UTF_8)
                    + "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

            if (senderId != null && !senderId.isBlank()) {
                formData += "&from=" + URLEncoder.encode(senderId, StandardCharsets.UTF_8);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AT_URL))
                    .header("Accept", "application/json")
                    .header("apiKey", apiKey)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            log.info("SMS sent to {}. Response: {}", phoneNumber, response.body());

        } catch (Exception e) {
            // Log but don't fail the request if SMS fails
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
        }
    }

    public void sendCartSummary(String phoneNumber, String customerName,
                                java.util.List<com.example.demo.models.CartItems> items,
                                java.math.BigDecimal cartTotal) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hi ").append(customerName).append(", your cart:\n");
        for (com.example.demo.models.CartItems item : items) {
            sb.append("- ").append(item.getProduct().getName())
                    .append(" x").append(item.getQuantity())
                    .append(" @ KES ").append(item.getProduct().getPrice())
                    .append("\n");
        }
        sb.append("Total: KES ").append(cartTotal);

        sendSms(phoneNumber, sb.toString());
    }
}
