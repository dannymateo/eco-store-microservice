package com.itm.api_gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nats")
public record NatsGatewayProperties(
        String url,
        long requestTimeoutMs,
        Subject subject
) {
    public record Subject(
            Product product,
            Cart cart,
            Checkout checkout
    ) {
    }

    public record Product(
            String create,
            String get,
            String list,
            String update,
            String delete
    ) {
    }

    public record Cart(
            String addProduct,
            String removeProduct,
            String get,
            String checkout
    ) {
    }

    public record Checkout(
            String init,
            String confirm
    ) {
    }
}
