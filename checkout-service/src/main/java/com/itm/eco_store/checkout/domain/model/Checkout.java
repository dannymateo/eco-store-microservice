package com.itm.eco_store.checkout.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Checkout {
    private Long id;
    private String cartId;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentProvider;
    private String paymentReference;
    private String approvalUrl;
    private CheckoutStatus status;
    private Instant createdAt;
    private List<CheckoutItem> items = new ArrayList<>();

    public Checkout() {
    }

    public Checkout(Long id, String cartId, BigDecimal totalAmount, String currency, String paymentProvider,
                    String paymentReference, String approvalUrl, CheckoutStatus status, Instant createdAt, List<CheckoutItem> items) {
        this.id = id;
        this.cartId = cartId;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.paymentProvider = paymentProvider;
        this.paymentReference = paymentReference;
        this.approvalUrl = approvalUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.items = items == null ? new ArrayList<>() : items;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(String paymentProvider) { this.paymentProvider = paymentProvider; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public String getApprovalUrl() { return approvalUrl; }
    public void setApprovalUrl(String approvalUrl) { this.approvalUrl = approvalUrl; }
    public CheckoutStatus getStatus() { return status; }
    public void setStatus(CheckoutStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<CheckoutItem> getItems() { return items; }
    public void setItems(List<CheckoutItem> items) { this.items = items == null ? new ArrayList<>() : items; }
}
