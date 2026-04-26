package com.itm.eco_store.cart.application.service;

import com.itm.eco_store.cart.application.port.in.IAddProductToCartUseCase;
import com.itm.eco_store.cart.application.port.in.ICheckoutCartUseCase;
import com.itm.eco_store.cart.application.port.in.IClearCartUseCase;
import com.itm.eco_store.cart.application.port.in.IGetCartUseCase;
import com.itm.eco_store.cart.application.port.in.IRemoveProductFromCartUseCase;
import com.itm.eco_store.cart.application.port.out.CartEventPort;
import com.itm.eco_store.cart.application.port.out.CartRepositoryPort;
import com.itm.eco_store.cart.application.port.out.ProductCatalogPort;
import com.itm.eco_store.cart.domain.model.Cart;
import com.itm.eco_store.cart.domain.policy.CartExpirationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartApplicationService implements
        IAddProductToCartUseCase, IRemoveProductFromCartUseCase, IGetCartUseCase, ICheckoutCartUseCase, IClearCartUseCase {

    private final CartRepositoryPort cartRepositoryPort;
    private final ProductCatalogPort productCatalogPort;
    private final CartEventPort cartEventPort;

    @Override
    public Cart addProduct(String cartId, Long productId, int quantity) {
        Cart cart = cartRepositoryPort.findById(cartId).orElseGet(() -> Cart.open(cartId));
        var productInfo = productCatalogPort.getProductById(productId);
        cart.addProduct(productInfo, quantity);
        return cartRepositoryPort.save(cart, CartExpirationPolicy.resolveTtl(cart));
    }

    @Override
    public Cart removeProduct(String cartId, Long productId) {
        Cart cart = findExistingCart(cartId);
        cart.removeProduct(productId);
        return cartRepositoryPort.save(cart, CartExpirationPolicy.resolveTtl(cart));
    }

    @Override
    public Cart getCart(String cartId) {
        return findExistingCart(cartId);
    }

    @Override
    public Cart checkout(String cartId) {
        Cart cart = findExistingCart(cartId);
        cart.checkout();
        Cart saved = cartRepositoryPort.save(cart, CartExpirationPolicy.resolveTtl(cart));
        cartEventPort.publishCartCheckedOut(saved);
        return saved;
    }

    @Override
    public Cart clearCart(String cartId) {
        Cart cart = findExistingCart(cartId);
        cart.clear();
        return cartRepositoryPort.save(cart, CartExpirationPolicy.resolveTtl(cart));
    }

    private Cart findExistingCart(String cartId) {
        return cartRepositoryPort.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado: " + cartId));
    }
}
