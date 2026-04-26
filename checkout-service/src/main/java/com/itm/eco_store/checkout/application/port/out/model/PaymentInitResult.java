package com.itm.eco_store.checkout.application.port.out;

public record PaymentInitResult(
        String provider,
        String orderReference,
        String approvalUrl
) {
}
