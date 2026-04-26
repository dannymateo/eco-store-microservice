package com.itm.eco_store.checkout.application.port.in;

import com.itm.eco_store.checkout.domain.model.Checkout;

public interface ProcessCheckoutUseCase {
    Checkout process(String cartId, String paymentMethod, String payerEmail);
}
