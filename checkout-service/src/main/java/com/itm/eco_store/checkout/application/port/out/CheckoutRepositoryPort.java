package com.itm.eco_store.checkout.application.port.out;

import com.itm.eco_store.checkout.domain.model.Checkout;

import java.util.Optional;

public interface CheckoutRepositoryPort {
    Checkout save(Checkout checkout);
    Optional<Checkout> findByPaymentReference(String paymentReference);
}
