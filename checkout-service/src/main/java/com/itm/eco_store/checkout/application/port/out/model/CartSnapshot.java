package com.itm.eco_store.checkout.application.port.out;

import java.math.BigDecimal;
import java.util.List;

public record CartSnapshot(
        String id,
        List<CartItemSnapshot> items,
        BigDecimal totalAmount,
        String currency
) {
}
