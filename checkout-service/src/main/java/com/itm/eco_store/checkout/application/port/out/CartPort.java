package com.itm.eco_store.checkout.application.port.out;

public interface CartPort {
    CartSnapshot getCart(String cartId);
    void clearCart(String cartId);
}
