package com.itm.eco_store.checkout.application.service;

import com.itm.eco_store.checkout.application.port.in.ConfirmCheckoutUseCase;
import com.itm.eco_store.checkout.application.port.in.ProcessCheckoutUseCase;
import com.itm.eco_store.checkout.application.port.out.CartPort;
import com.itm.eco_store.checkout.application.port.out.CheckoutRepositoryPort;
import com.itm.eco_store.checkout.application.port.out.PaymentPort;
import com.itm.eco_store.checkout.application.port.out.PaymentInitResult;
import com.itm.eco_store.checkout.application.port.out.PaymentRequest;
import com.itm.eco_store.checkout.application.port.out.ProductPort;
import com.itm.eco_store.checkout.domain.model.Checkout;
import com.itm.eco_store.checkout.domain.model.CheckoutItem;
import com.itm.eco_store.checkout.domain.model.CheckoutStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutApplicationService implements ProcessCheckoutUseCase, ConfirmCheckoutUseCase {

    private final CartPort cartPort;
    private final ProductPort productPort;
    private final List<PaymentPort> paymentPorts;
    private final CheckoutRepositoryPort checkoutRepositoryPort;

    @Override
    @Transactional
    public Checkout process(String cartIdValue, String paymentMethodValue, String payerEmailValue) {
        String cartId = require(cartIdValue, "cartId es obligatorio");
        String paymentMethod = require(paymentMethodValue, "paymentMethod es obligatorio");
        String payerEmail = require(payerEmailValue, "payerEmail es obligatorio");

        var cart = cartPort.getCart(cartId);
        if (cart.items() == null || cart.items().isEmpty()) {
            throw new IllegalStateException("No se puede procesar checkout de un carrito vacio");
        }
        productPort.validateStock(cart.items());

        PaymentPort paymentPort = paymentPorts.stream()
                .filter(port -> port.supports(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Metodo de pago no soportado: " + paymentMethod));

        PaymentInitResult paymentInit = paymentPort.createOrder(new PaymentRequest(
                cartId,
                cart.totalAmount(),
                cart.currency(),
                paymentMethod,
                payerEmail
        ));

        List<CheckoutItem> detail = new ArrayList<>();
        for (var item : cart.items()) {
            detail.add(new CheckoutItem(
                    item.productId(),
                    item.name(),
                    item.quantity(),
                    item.unitPrice(),
                    item.unitPrice().multiply(java.math.BigDecimal.valueOf(item.quantity())),
                    item.currency()
            ));
        }

        var created = checkoutRepositoryPort.save(new Checkout(
                null,
                cartId,
                cart.totalAmount(),
                cart.currency(),
                paymentInit.provider(),
                paymentInit.orderReference(),
                paymentInit.approvalUrl(),
                CheckoutStatus.CREATED,
                Instant.now(),
                detail
        ));
        created.setApprovalUrl(paymentInit.approvalUrl());
        return created;
    }

    @Override
    @Transactional
    public Checkout confirm(String paymentMethodValue, String orderReferenceValue) {
        String orderReference = require(orderReferenceValue, "orderReference es obligatorio");
        String paymentMethod = (paymentMethodValue == null || paymentMethodValue.isBlank())
                ? "PAYPAL"
                : paymentMethodValue.trim();

        var checkout = checkoutRepositoryPort.findByPaymentReference(orderReference)
                .orElseThrow(() -> new IllegalArgumentException("Checkout no encontrado para orderReference: " + orderReference));

        if (checkout.getStatus() == CheckoutStatus.PAID) {
            return checkout;
        }

        PaymentPort paymentPort = paymentPorts.stream()
                .filter(port -> port.supports(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Metodo de pago no soportado: " + paymentMethod));

        var cart = cartPort.getCart(checkout.getCartId());
        var payment = paymentPort.captureOrder(paymentMethod, orderReference);

        if (payment.approved()) {
            productPort.decrementStock(cart.items());
        }

        checkout.setStatus(payment.approved() ? CheckoutStatus.PAID : CheckoutStatus.FAILED);
        var paid = checkoutRepositoryPort.save(checkout);

        if (!payment.approved()) {
            throw new IllegalStateException("Pago rechazado. referencia=" + orderReference);
        }

        cartPort.clearCart(checkout.getCartId());

        return paid;
    }

    private String require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
