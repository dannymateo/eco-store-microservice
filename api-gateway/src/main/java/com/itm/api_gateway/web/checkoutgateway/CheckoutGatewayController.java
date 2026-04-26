package com.itm.api_gateway.web.checkoutgateway;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutGatewayController {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String checkoutWebhookBaseUrl;

    public CheckoutGatewayController(
            ObjectMapper objectMapper,
            @Value("${checkout.webhook-base-url}") String checkoutWebhookBaseUrl
    ) {
        this.objectMapper = objectMapper;
        this.checkoutWebhookBaseUrl = checkoutWebhookBaseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @PostMapping("/paypal/webhook")
    @Operation(summary = "Webhook PayPal (pass-through)", description = "Reenvia el webhook a checkout-service para su procesamiento.")
    public ResponseEntity<?> paypalWebhook(@RequestBody JsonNode payload) {
        try {
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(joinUrl(checkoutWebhookBaseUrl, "/api/v1/checkout/paypal/webhook")))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String responseBody = response.body();
            if (responseBody == null || responseBody.isBlank()) {
                responseBody = "{\"received\":true}";
            }
            return ResponseEntity.status(status).body(objectMapper.readTree(responseBody));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "No se pudo reenviar webhook a checkout-service: " + ex.getMessage()));
        }
    }

    private String joinUrl(String baseUrl, String path) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + path;
    }
}
