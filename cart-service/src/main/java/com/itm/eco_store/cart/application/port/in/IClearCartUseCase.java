package com.itm.eco_store.cart.application.port.in;

import com.itm.eco_store.cart.domain.model.Cart;

public interface IClearCartUseCase {
    Cart clearCart(String cartId);
}
