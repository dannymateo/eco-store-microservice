package com.itm.eco_store.checkout.infrastructure.adapter.in.messaging.dto;

public record ProcessCheckoutCommandMessage(
        String cartId,
        String paymentMethod,
        String payerEmail
) {
}
