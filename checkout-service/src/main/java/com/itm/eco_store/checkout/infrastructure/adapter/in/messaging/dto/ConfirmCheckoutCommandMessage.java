package com.itm.eco_store.checkout.infrastructure.adapter.in.messaging.dto;

public record ConfirmCheckoutCommandMessage(
        String paymentMethod,
        String orderReference
) {
}
