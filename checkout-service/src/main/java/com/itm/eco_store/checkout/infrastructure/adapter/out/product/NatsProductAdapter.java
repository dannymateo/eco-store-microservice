package com.itm.eco_store.checkout.infrastructure.adapter.out.product;

import com.itm.eco_store.checkout.application.port.out.ProductPort;
import com.itm.eco_store.checkout.application.port.out.CartItemSnapshot;
import com.itm.eco_store.checkout.infrastructure.config.NatsCheckoutProperties;
import io.nats.client.Connection;
import io.nats.client.Message;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class NatsProductAdapter implements ProductPort {

    private final Connection connection;
    private final ObjectMapper objectMapper;
    private final NatsCheckoutProperties properties;

    public NatsProductAdapter(Connection connection, ObjectMapper objectMapper, NatsCheckoutProperties properties) {
        this.connection = connection;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void validateStock(List<CartItemSnapshot> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (CartItemSnapshot item : items) {
            if (item == null || item.productId() == null || item.quantity() <= 0) {
                continue;
            }
            JsonNode current = requestData(properties.subject().product().get(), Map.of("id", item.productId()));
            int currentStock = current.path("stock").asInt(0);
            if (currentStock < item.quantity()) {
                throw new IllegalStateException("Stock insuficiente para producto: " + item.productId());
            }
        }
    }

    @Override
    public void decrementStock(List<CartItemSnapshot> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (CartItemSnapshot item : items) {
            if (item == null || item.productId() == null || item.quantity() <= 0) {
                continue;
            }
            JsonNode current = requestData(properties.subject().product().get(), Map.of("id", item.productId()));
            int currentStock = current.path("stock").asInt(0);
            int newStock = currentStock - item.quantity();
            if (newStock < 0) {
                throw new IllegalStateException("Stock insuficiente para producto: " + item.productId());
            }

            Map<String, Object> updatePayload = Map.of(
                    "id", item.productId(),
                    "name", current.path("name").asText(),
                    "description", current.path("description").asText(),
                    "category", current.path("category").asText(),
                    "originalPrice", current.path("originalPrice").decimalValue(),
                    "stock", newStock
            );
            requestData(properties.subject().product().update(), updatePayload);
        }
    }

    private JsonNode requestData(String subject, Object payload) {
        try {
            byte[] request = objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
            Message reply = connection.request(subject, request, Duration.ofSeconds(5));
            if (reply == null) {
                throw new IllegalStateException("Sin respuesta del servicio de productos");
            }
            JsonNode envelope = objectMapper.readTree(reply.getData());
            if (!envelope.path("success").asBoolean(false)) {
                throw new IllegalStateException(envelope.path("error").asText("Error en servicio de productos"));
            }
            return envelope.path("data");
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo consultar product-service: " + e.getMessage(), e);
        }
    }
}
