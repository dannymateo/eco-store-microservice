package com.itm.eco_store.checkout.infrastructure.adapter.out.cart;

import com.itm.eco_store.checkout.application.port.out.CartPort;
import com.itm.eco_store.checkout.application.port.out.CartItemSnapshot;
import com.itm.eco_store.checkout.application.port.out.CartSnapshot;
import com.itm.eco_store.checkout.infrastructure.config.NatsCheckoutProperties;
import io.nats.client.Connection;
import io.nats.client.Message;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class NatsCartAdapter implements CartPort {

    private final Connection connection;
    private final ObjectMapper objectMapper;
    private final NatsCheckoutProperties properties;

    public NatsCartAdapter(Connection connection, ObjectMapper objectMapper, NatsCheckoutProperties properties) {
        this.connection = connection;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public CartSnapshot getCart(String cartId) {
        JsonNode data = requestData(properties.subject().cart().get(), Map.of("cartId", cartId));

        List<CartItemSnapshot> items = new ArrayList<>();
        BigDecimal computedTotal = BigDecimal.ZERO;
        String detectedCurrency = "USD";
        for (JsonNode itemNode : data.path("items")) {
            JsonNode product = itemNode.path("product");
            int quantity = itemNode.path("quantity").asInt();

            BigDecimal unitPrice = product.path("price").path("value").decimalValue();
            computedTotal = computedTotal.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));

            String currency = product.path("price").path("currency").asText();
            if (currency != null && !currency.isBlank()) {
                detectedCurrency = currency;
            }

            items.add(new CartItemSnapshot(
                    product.path("id").asLong(),
                    product.path("name").asText(),
                    quantity,
                    unitPrice,
                    currency
            ));
        }

        JsonNode totalNode = data.path("total");
        BigDecimal totalAmount = totalNode.path("value").isMissingNode()
                ? computedTotal
                : totalNode.path("value").decimalValue();
        String currency = totalNode.path("currency").asText(detectedCurrency);

        return new CartSnapshot(
                data.path("id").asText(),
                items,
                totalAmount,
                currency
        );
    }

    @Override
    public void clearCart(String cartId) {
        requestData(properties.subject().cart().clear(), Map.of("cartId", cartId));
    }

    private JsonNode requestData(String subject, Object payload) {
        try {
            byte[] request = objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
            Message reply = connection.request(subject, request, Duration.ofSeconds(3));
            if (reply == null) {
                throw new IllegalStateException("Sin respuesta del servicio de carrito");
            }
            JsonNode envelope = objectMapper.readTree(reply.getData());
            if (!envelope.path("success").asBoolean(false)) {
                throw new IllegalStateException(envelope.path("error").asText("Error en servicio de carrito"));
            }
            return envelope.path("data");
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo consultar cart-service: " + e.getMessage(), e);
        }
    }
}
