package com.itm.eco_store.checkout.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nats")
public record NatsCheckoutProperties(
        String url,
        Subject subject
) {
    public record Subject(
            Checkout checkout,
            Cart cart,
            Product product
    ) {
    }

    public record Checkout(
            String init,
            String confirm
    ) {
    }

    public record Cart(
            String get,
            String clear
    ) {
    }

    public record Product(
            String get,
            String update
    ) {
    }
}
