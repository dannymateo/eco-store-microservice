package com.itm.eco_store.checkout.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paypal")
public record PaypalProperties(
        String clientId,
        String clientSecret,
        String baseUrl,
        String returnUrl,
        String cancelUrl
) {
}
