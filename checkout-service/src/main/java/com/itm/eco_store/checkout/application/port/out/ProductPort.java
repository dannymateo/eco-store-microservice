package com.itm.eco_store.checkout.application.port.out;

import java.util.List;

public interface ProductPort {
    void validateStock(List<CartItemSnapshot> items);
    void decrementStock(List<CartItemSnapshot> items);
}
