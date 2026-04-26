package com.itm.eco_store.cart.infrastructure.adapter.in.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nats")
public record NatsCartProperties(
        String url,
        Subject subject
) {
    public record Subject(
            Cart cart,
            Product product
    ) {
    }

    public record Cart(
            String addProduct,
            String removeProduct,
            String get,
            String checkout,
            String checkoutEvent,
            String clear
    ) {
    }

    public record Product(
            String get
    ) {
    }
}
