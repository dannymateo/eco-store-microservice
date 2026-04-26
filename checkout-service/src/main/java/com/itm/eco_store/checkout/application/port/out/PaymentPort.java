package com.itm.eco_store.checkout.application.port.out;

public interface PaymentPort {
    boolean supports(String paymentMethod);
    PaymentInitResult createOrder(PaymentRequest request);
    PaymentResult captureOrder(String paymentMethod, String orderReference);
}
