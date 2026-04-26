package com.itm.eco_store.cart.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    private String id;
    private boolean checkedOut;
    private List<CartItem> items;

    public static Cart open(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del carrito es obligatorio");
        }
        return new Cart(id.trim(), false, new ArrayList<>());
    }

    public void addProduct(ProductInfo product, int quantity) {
        ensureOpen();
        validateQuantity(quantity);
        Optional<CartItem> existing = items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();
        if (existing.isPresent()) {
            int newQuantity = existing.get().getQuantity() + quantity;
            validateStock(product, newQuantity);
            existing.get().increaseQuantity(quantity);
            return;
        }
        validateStock(product, quantity);
        items.add(new CartItem(product, quantity));
    }

    public void removeProduct(Long productId) {
        ensureOpen();
        boolean removed = false;
        for (Iterator<CartItem> iterator = items.iterator(); iterator.hasNext(); ) {
            CartItem item = iterator.next();
            if (item.getProduct().getId().equals(productId)) {
                iterator.remove();
                removed = true;
                break;
            }
        }
        if (!removed) {
            throw new IllegalArgumentException("Producto no existe en el carrito: " + productId);
        }
    }

    public Money total() {
        if (items.isEmpty()) {
            return Money.of(BigDecimal.ZERO, "USD");
        }
        String currency = items.getFirst().getProduct().getPrice().getCurrency();
        BigDecimal total = items.stream()
                .map(CartItem::lineTotal)
                .map(Money::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Money.of(total, currency);
    }

    public void checkout() {
        ensureOpen();
        if (items.isEmpty()) {
            throw new IllegalStateException("No se puede cerrar un carrito sin items");
        }
        this.checkedOut = true;
    }

    public void clear() {
        items.clear();
        this.checkedOut = false;
    }

    private void ensureOpen() {
        if (checkedOut) {
            throw new IllegalStateException("El carrito ya fue cerrado");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
    }

    private void validateStock(ProductInfo product, int requestedQuantity) {
        Integer stock = product.getStock();
        if (stock == null || stock < requestedQuantity) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + product.getId());
        }
    }
}
