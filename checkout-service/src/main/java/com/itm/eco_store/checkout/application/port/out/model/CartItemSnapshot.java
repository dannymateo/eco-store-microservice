package com.itm.eco_store.checkout.application.port.out;

import java.math.BigDecimal;

public record CartItemSnapshot(
        Long productId,
        String name,
        int quantity,
        BigDecimal unitPrice,
        String currency
) {
}
