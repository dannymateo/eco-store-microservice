package com.itm.eco_store.checkout.application.port.in;

import com.itm.eco_store.checkout.domain.model.Checkout;

public interface ConfirmCheckoutUseCase {
    Checkout confirm(String paymentMethod, String orderReference);
}
