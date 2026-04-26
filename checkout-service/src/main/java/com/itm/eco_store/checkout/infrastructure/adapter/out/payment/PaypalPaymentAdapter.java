package com.itm.eco_store.checkout.infrastructure.adapter.out.payment;

import com.itm.eco_store.checkout.application.port.out.PaymentPort;
import com.itm.eco_store.checkout.application.port.out.PaymentInitResult;
import com.itm.eco_store.checkout.application.port.out.PaymentRequest;
import com.itm.eco_store.checkout.application.port.out.PaymentResult;
import com.itm.eco_store.checkout.infrastructure.config.PaypalProperties;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Component
public class PaypalPaymentAdapter implements PaymentPort {

    private final PaypalProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public PaypalPaymentAdapter(PaypalProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public boolean supports(String paymentMethod) {
        return "PAYPAL".equalsIgnoreCase(paymentMethod);
    }

    @Override
    public PaymentInitResult createOrder(PaymentRequest request) {
        if (!supports(request.paymentMethod())) {
            throw new IllegalArgumentException("Metodo de pago no soportado por PayPal: " + request.paymentMethod());
        }
        validateConfiguration();
        try {
            String accessToken = fetchAccessToken();
            return createOrder(accessToken, request);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo crear orden PayPal: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResult captureOrder(String paymentMethod, String orderReference) {
        if (!supports(paymentMethod)) {
            throw new IllegalArgumentException("Metodo de pago no soportado por PayPal: " + paymentMethod);
        }
        validateConfiguration();
        if (orderReference == null || orderReference.isBlank()) {
            throw new IllegalArgumentException("orderReference es obligatorio para captura");
        }
        try {
            String accessToken = fetchAccessToken();
            String captureId = captureOrderInternal(accessToken, orderReference.trim());
            return new PaymentResult(true, "PAYPAL", captureId);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo procesar pago con PayPal: " + e.getMessage(), e);
        }
    }

    private String fetchAccessToken() throws Exception {
        String credentials = properties.clientId() + ":" + properties.clientSecret();
        String basic = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(joinUrl("/v1/oauth2/token")))
                .header("Authorization", "Basic " + basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=" + URLEncoder.encode("client_credentials", StandardCharsets.UTF_8)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("PayPal token error. status=" + response.statusCode() + " body=" + response.body());
        }
        JsonNode node = objectMapper.readTree(response.body());
        String token = node.path("access_token").asText(null);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("PayPal no devolvio access_token");
        }
        return token;
    }

    private PaymentInitResult createOrder(String accessToken, PaymentRequest paymentRequest) throws Exception {
        Map<String, Object> payload = Map.of(
                "intent", "CAPTURE",
                "purchase_units", new Object[]{
                        Map.of(
                                "reference_id", paymentRequest.cartId(),
                                "custom_id", paymentRequest.payerEmail(),
                                "amount", Map.of(
                                        "currency_code", paymentRequest.currency(),
                                        "value", paymentRequest.amount().toPlainString()
                                )
                        )
                },
                "application_context", Map.of(
                        "return_url", properties.returnUrl(),
                        "cancel_url", properties.cancelUrl()
                )
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(joinUrl("/v2/checkout/orders")))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("PayPal create order error. status=" + response.statusCode() + " body=" + response.body());
        }
        JsonNode node = objectMapper.readTree(response.body());
        String orderId = node.path("id").asText(null);
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalStateException("PayPal no devolvio order id");
        }
        String approveUrl = null;
        for (JsonNode link : node.path("links")) {
            if ("approve".equalsIgnoreCase(link.path("rel").asText())) {
                approveUrl = link.path("href").asText(null);
                break;
            }
        }
        if (approveUrl == null || approveUrl.isBlank()) {
            throw new IllegalStateException("PayPal no devolvio approve url");
        }
        return new PaymentInitResult("PAYPAL", orderId, approveUrl);
    }

    private String captureOrderInternal(String accessToken, String orderId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(joinUrl("/v2/checkout/orders/" + orderId + "/capture")))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("PayPal capture error. status=" + response.statusCode() + " body=" + response.body());
        }
        JsonNode root = objectMapper.readTree(response.body());
        String status = root.path("status").asText("");
        if (!"COMPLETED".equalsIgnoreCase(status)) {
            throw new IllegalStateException("PayPal capture no completado. status=" + status);
        }

        JsonNode captures = root.path("purchase_units").path(0).path("payments").path("captures");
        if (!captures.isArray() || captures.isEmpty()) {
            return orderId;
        }
        String captureId = captures.get(0).path("id").asText(null);
        return (captureId == null || captureId.isBlank()) ? orderId : captureId;
    }

    private String joinUrl(String path) {
        String base = properties.baseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + path;
    }

    private void validateConfiguration() {
        if (properties.clientId() == null || properties.clientId().isBlank()) {
            throw new IllegalStateException("PAYPAL_CLIENT_ID no configurado");
        }
        if (properties.clientSecret() == null || properties.clientSecret().isBlank()) {
            throw new IllegalStateException("PAYPAL_CLIENT_SECRET no configurado");
        }
        if (properties.baseUrl() == null || properties.baseUrl().isBlank()) {
            throw new IllegalStateException("PAYPAL_BASE_URL no configurado");
        }
        if (properties.returnUrl() == null || properties.returnUrl().isBlank()) {
            throw new IllegalStateException("PAYPAL_RETURN_URL no configurado");
        }
        if (properties.cancelUrl() == null || properties.cancelUrl().isBlank()) {
            throw new IllegalStateException("PAYPAL_CANCEL_URL no configurado");
        }
    }
}
