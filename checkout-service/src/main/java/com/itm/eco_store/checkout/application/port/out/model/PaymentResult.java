package com.itm.eco_store.checkout.application.port.out;

public record PaymentResult(
        boolean approved,
        String provider,
        String reference
) {
}
