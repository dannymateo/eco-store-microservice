package com.itm.eco_store.checkout.application.port.out;

import java.math.BigDecimal;

public record PaymentRequest(
        String cartId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String payerEmail
) {
}
